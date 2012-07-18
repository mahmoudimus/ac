package com.atlassian.labs.remoteapps.container;

import com.atlassian.labs.remoteapps.api.DescriptorGenerator;
import com.atlassian.labs.remoteapps.container.services.DescriptorGeneratorServiceFactory;
import com.atlassian.labs.remoteapps.container.services.sal.RemoteAppsApplicationPropertiesServiceFactory;
import com.atlassian.labs.remoteapps.container.services.sal.RemoteAppsPluginSettingsFactory;
import com.atlassian.labs.remoteapps.container.util.ZipWriter;
import com.atlassian.plugin.DefaultModuleDescriptorFactory;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.event.impl.DefaultPluginEventManager;
import com.atlassian.plugin.hostcontainer.DefaultHostContainer;
import com.atlassian.plugin.loaders.DirectoryScanner;
import com.atlassian.plugin.loaders.FileListScanner;
import com.atlassian.plugin.loaders.PluginLoader;
import com.atlassian.plugin.loaders.ScanningPluginLoader;
import com.atlassian.plugin.loaders.classloading.Scanner;
import com.atlassian.plugin.manager.DefaultPluginManager;
import com.atlassian.plugin.manager.store.MemoryPluginPersistentStateStore;
import com.atlassian.plugin.module.ClassPrefixModuleFactory;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.module.PrefixDelegatingModuleFactory;
import com.atlassian.plugin.osgi.container.OsgiContainerManager;
import com.atlassian.plugin.osgi.container.OsgiPersistentCache;
import com.atlassian.plugin.osgi.container.felix.FelixOsgiContainerManager;
import com.atlassian.plugin.osgi.container.impl.DefaultOsgiPersistentCache;
import com.atlassian.plugin.osgi.container.impl.DefaultPackageScannerConfiguration;
import com.atlassian.plugin.osgi.factory.OsgiBundleFactory;
import com.atlassian.plugin.osgi.factory.OsgiPluginFactory;
import com.atlassian.plugin.osgi.hostcomponents.ComponentRegistrar;
import com.atlassian.plugin.osgi.hostcomponents.ContextClassLoaderStrategy;
import com.atlassian.plugin.osgi.hostcomponents.HostComponentProvider;
import com.atlassian.plugin.osgi.module.BeanPrefixModuleFactory;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.Charset;
import java.util.*;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Arrays.asList;

/**
 *
 */
public class Container
{
    private static final Logger log = LoggerFactory.getLogger(Container.class);
    private final DefaultPluginManager pluginManager;
    private final HttpServer httpServer;
    public static final Set<URI> AUTOREGISTER_HOSTS = ImmutableSet.of(
            URI.create("http://localhost:1990/confluence"),
            URI.create("http://localhost:2990/jira"),
            URI.create("http://localhost:5990/refapp"));

    public Container(HttpServer server, String[] apps) throws FileNotFoundException
    {
        this.httpServer = server;

        // todo: this should use the plugin api, but it doesn't allow setting of plugin loaders right now
        final DefaultPackageScannerConfiguration scannerConfig = new DefaultPackageScannerConfiguration(determineVersion());

        final List<String> packageIncludes = new ArrayList<String>(scannerConfig.getPackageIncludes());
        packageIncludes.add("org.bouncycastle*");
        packageIncludes.add("org.dom4j*");
        packageIncludes.add("org.apache.log4j*");
        packageIncludes.add("org.slf4j*");
        packageIncludes.add("javax.servlet*");
        packageIncludes.add("com.google.common.*");
        packageIncludes.add("net.oauth*");
        packageIncludes.add("org.json");
        packageIncludes.add("org.mozilla.javascript*");
        packageIncludes.add("org.yaml*");
        packageIncludes.add("org.eclipse.jetty.*");
        packageIncludes.add("org.jruby*");
        packageIncludes.add("com.atlassian.sal*");
        packageIncludes.add("com.samskivert.*");

        packageIncludes.remove("org.jfree.*");
        packageIncludes.remove("org.joda.*");
        packageIncludes.remove("org.w3c.*");
        packageIncludes.remove("org.xml.*");
        packageIncludes.remove("org.ofbiz.*");
        packageIncludes.remove("webwork.*");
        packageIncludes.remove("org.quartz");
        packageIncludes.remove("org.quartz.*");
        packageIncludes.remove("com.opensymphony.*");
        packageIncludes.remove("org.tuckey.web.filters.urlrewrite.*");

        scannerConfig.setPackageIncludes(packageIncludes);
        scannerConfig.setPackageVersions(new HashMap<String,String>() {{
            put("javax.servlet", "2.5");
            put("javax.servlet.http", "2.5");
            put("org.slf4j.*", "1.6.4");
            put("org.slf4j", "1.6.4");
            put("org.apache.commons.lang", "2.4");
            put("org.apache.commons.lang.*", "2.4");
            put("org.apache.commons.collections", "3.2");
            put("org.apache.commons.collections.*", "3.2");
            put("com.google.common.*", getGoogleGuavaVersion());
        }});

        OsgiPersistentCache osgiCache = new DefaultOsgiPersistentCache(mkdir(".cache/osgi"));
        Map<Class<?>, Object> hostComponents = newHashMap();
        PluginEventManager pluginEventManager = new DefaultPluginEventManager();
        OsgiContainerManager osgiContainerManager = new FelixOsgiContainerManager(
                                osgiCache,
                                scannerConfig,
                                new ContainerHostComponentProvider(hostComponents),
                                pluginEventManager);

        OsgiBundleFactory bundleFactory = new OsgiBundleFactory(osgiContainerManager, pluginEventManager);
        OsgiPluginFactory osgiPluginDeployer = new OsgiPluginFactory(
                        PluginAccessor.Descriptor.FILENAME,
                        ImmutableSet.of("remoteapp"),
                        osgiCache,
                        osgiContainerManager,
                        pluginEventManager);

        Scanner scanner = null;
        if (apps.length == 0)
        {
            File appDir = mkdir("apps");
            scanner = new DirectoryScanner(appDir);
        }
        else
        {
            List<File> files = newArrayList();
            for (String app : apps)
            {
                File appFile = new File(app);
                if (!appFile.exists())
                {
                    throw new FileNotFoundException("App '" + app + "' not found");
                }
                if (appFile.isDirectory())
                {
                    System.setProperty("plugin.resource.directories", appFile.getAbsolutePath());
                    File appAsZip = zipAppDirectory(appFile);
                    files.add(appAsZip);
                }
                else
                {
                    files.add(appFile);
                }
            }
            scanner = new FileListScanner(files);
        }
        PluginLoader appPluginLoader = new ScanningPluginLoader(scanner, asList(osgiPluginDeployer, bundleFactory), pluginEventManager);

        final DefaultHostContainer hostContainer = new DefaultHostContainer();
        pluginManager = new DefaultPluginManager(
                new MemoryPluginPersistentStateStore(),
                asList(appPluginLoader),
                new DefaultModuleDescriptorFactory(hostContainer),
                pluginEventManager
        );

        hostComponents.put(ApplicationProperties.class, new RemoteAppsApplicationPropertiesServiceFactory(server));
        hostComponents.put(PluginSettingsFactory.class, new RemoteAppsPluginSettingsFactory());

        hostComponents.put(DescriptorGenerator.class, new DescriptorGeneratorServiceFactory(pluginManager, server));
        hostComponents.put(PluginAccessor.class, pluginManager);
        hostComponents.put(PluginController.class, pluginManager);
        hostComponents.put(PluginEventManager.class, pluginEventManager);
        hostComponents.put(ModuleFactory.class, new PrefixDelegatingModuleFactory(
                ImmutableSet.of(new ClassPrefixModuleFactory(hostContainer),
                        new BeanPrefixModuleFactory())));
    }

    private File zipAppDirectory(File appFile)
    {
        try
        {
            return ZipWriter.zipAppIntoPluginJar(appFile);
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("Unable to zip up app: " + appFile.getPath(), e);
        }
    }

    public void start()
    {
        pluginManager.init();
        tryToAutoRegister(httpServer.getContextNames());
    }

    private void tryToAutoRegister(Set<String> appKeys)
    {
        for (URI host : AUTOREGISTER_HOSTS)
        {
            Socket socket = null;
            try
            {
                log.debug("Trying to register at " + host);
                socket = new Socket(host.getHost(), host.getPort());
                for (String appKey : appKeys)
                {
                    registerApp(host, appKey);
                    log.info("Registered '" + appKey + "' at " + host);
                }
            }
            catch (UnknownHostException e)
            {
                throw new RuntimeException("Not possible", e);
            }
            catch (IOException e)
            {
                // ignore, and try another
            }
            finally
            {
                try
                {
                    if (socket != null)
                    {
                        socket.close();
                    }
                }
                catch (IOException e)
                {
                    // ignore
                }
            }
        }
    }

    private void registerApp(URI host, String appKey) throws IOException
    {
        URL url = new URL(host.toString() + "/rest/remoteapps/latest/installer");
        OutputStream out = null;
        try{
            HttpURLConnection uc = (HttpURLConnection) url.openConnection();
            uc.setDoOutput(true);
            uc.setDoInput(true);
            String authorizationString = "Basic " + DatatypeConverter.printBase64Binary(
                    "admin:admin".getBytes(Charset.defaultCharset()));
            uc.setRequestProperty ("Authorization", authorizationString);
            uc.setRequestMethod("POST");
            uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            out = uc.getOutputStream();
            out.write(
                    ("url=" + URLEncoder.encode(httpServer.getLocalMountBaseUrl(appKey), "UTF-8") +
                            "&token=").getBytes(Charset.defaultCharset()));
            out.close();
            int response = uc.getResponseCode();
            log.debug("Registration response '" + response + "': " + IOUtils.toString(uc.getInputStream()));
            uc.getInputStream().close();
            // todo: handle errors
        }
        finally
        {
            IOUtils.closeQuietly(out);
        }
    }

    public void stop()
    {
        pluginManager.shutdown();
    }

    private File mkdir(String path)
    {
        File file = new File(path);
        if (!file.exists())
        {
            file.mkdirs();
        }
        return file;
    }

    private String getGoogleGuavaVersion()
    {
        return "1";
    }

    private String determineVersion()
    {
        return "1";
    }

    private static class ContainerHostComponentProvider implements HostComponentProvider
    {
        private final Map<Class<?>, Object> requiredServices;

        public ContainerHostComponentProvider(Map<Class<?>, Object> requiredServices)
        {
            this.requiredServices = requiredServices;
        }

        public void provide(ComponentRegistrar registrar)
        {
            for (Map.Entry<Class<?>, Object> entry : requiredServices.entrySet())
            {
                // register with the plugin ccl strategy to avoid
                // the ccl-switching proxy that breaks with ServiceFactory
                registrar.register(entry.getKey()).forInstance(entry.getValue()).withContextClassLoaderStrategy(
                        ContextClassLoaderStrategy.USE_PLUGIN);
            }
        }
    }
}
