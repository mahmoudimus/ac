package com.atlassian.labs.remoteapps.container;

import com.atlassian.labs.remoteapps.api.DescriptorGenerator;
import com.atlassian.labs.remoteapps.container.services.DescriptorGeneratorServiceFactory;
import com.atlassian.labs.remoteapps.container.services.sal
        .RemoteAppsApplicationPropertiesServiceFactory;
import com.atlassian.labs.remoteapps.container.services.sal.RemoteAppsPluginSettingsFactory;
import com.atlassian.labs.remoteapps.container.util.ZipWriter;
import com.atlassian.plugin.DefaultModuleDescriptorFactory;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.event.impl.DefaultPluginEventManager;
import com.atlassian.plugin.hostcontainer.DefaultHostContainer;
import com.atlassian.plugin.loaders.*;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Arrays.asList;

/**
 *
 */
public class Container
{

    private final DefaultPluginManager pluginManager;

    public Container(HttpServer server, String[] apps) throws FileNotFoundException
    {

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
        packageIncludes.add("org.ringojs.*");
        packageIncludes.add("org.jruby*");
        packageIncludes.add("com.atlassian.sal*");

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
