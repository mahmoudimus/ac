package com.atlassian.labs.remoteapps.container;

import com.atlassian.activeobjects.spi.DataSourceProvider;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.labs.remoteapps.api.DescriptorAccessor;
import com.atlassian.labs.remoteapps.api.HttpResourceMounter;
import com.atlassian.labs.remoteapps.api.PolygotDescriptorAccessor;
import com.atlassian.labs.remoteapps.api.services.PluginSettingsAsyncFactory;
import com.atlassian.labs.remoteapps.api.services.RequestContext;
import com.atlassian.labs.remoteapps.api.services.RequestContextServiceFactory;
import com.atlassian.labs.remoteapps.api.services.SignedRequestHandler;
import com.atlassian.labs.remoteapps.api.services.http.AsyncHttpClient;
import com.atlassian.labs.remoteapps.api.services.http.HostHttpClient;
import com.atlassian.labs.remoteapps.api.services.http.SyncHostHttpClient;
import com.atlassian.labs.remoteapps.api.services.http.SyncHostHttpClientServiceFactory;
import com.atlassian.labs.remoteapps.api.services.http.impl.DefaultAsyncHttpClient;
import com.atlassian.labs.remoteapps.api.services.http.impl.RequestKiller;
import com.atlassian.labs.remoteapps.api.services.impl.DefaultPluginSettingsAsyncFactory;
import com.atlassian.labs.remoteapps.api.services.impl.DefaultRequestContext;
import com.atlassian.labs.remoteapps.container.ao.RemoteAppsDataSourceProviderServiceFactory;
import com.atlassian.labs.remoteapps.container.internal.EnvironmentFactory;
import com.atlassian.labs.remoteapps.container.internal.properties.ResourcePropertiesLoader;
import com.atlassian.labs.remoteapps.container.services.ContainerHostHttpClientServiceFactory;
import com.atlassian.labs.remoteapps.container.services.ContainerHttpResourceMounterServiceFactory;
import com.atlassian.labs.remoteapps.container.services.OAuthSignedRequestHandlerServiceFactory;
import com.atlassian.labs.remoteapps.container.services.event.RemoteAppsEventPublisher;
import com.atlassian.labs.remoteapps.container.services.sal.RemoteAppsApplicationPropertiesServiceFactory;
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
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.core.transaction.NoOpTransactionTemplate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.atlassian.labs.remoteapps.container.util.AppRegister.registerApp;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;

public final class Container
{
    private static final Logger log = LoggerFactory.getLogger(Container.class);

    public static final Set<URI> AUTOREGISTER_HOSTS = ImmutableSet.of(URI.create("http://localhost:1990/confluence"),
            URI.create("http://localhost:2990/jira"), URI.create("http://localhost:5990/refapp"));

    private final DefaultPluginManager pluginManager;
    private final HttpServer httpServer;

    private DescriptorAccessor descriptorAccessor;
    private AppReloader appReloader;

    public Container(HttpServer server, String[] apps) throws FileNotFoundException
    {
        this.httpServer = server;

        // todo: this should use the plugin api, but it doesn't allow setting of plugin loaders right now
        final DefaultPackageScannerConfiguration scannerConfig = new DefaultPackageScannerConfiguration(determineVersion());

        final List<String> scannedPackageIncludes = scannerConfig.getPackageIncludes();
        Iterables.removeAll(scannedPackageIncludes, ImmutableList.builder()
                .add("com.opensymphony.*")
                .add("org.jfree.*")
                .add("org.joda.*")
                .add("org.ofbiz.*")
                .add("org.quartz")
                .add("org.quartz.*")
                .add("org.tuckey.web.filters.urlrewrite.*")
                .add("org.xml.*")
                .add("org.w3c.*")
                .add("webwork.*")
                .build());

        scannerConfig.setPackageIncludes(ImmutableList.<String>builder()
                .addAll(scannedPackageIncludes)
                .add("com.atlassian.activeobjects.spi*")
                .add("com.atlassian.event.api*")
                .add("com.atlassian.plugin*")
                .add("com.atlassian.sal*")
                .add("com.atlassian.security.random*")
                .add("com.google.common.*")
                .add("com.samskivert.*")
                .add("javax.servlet*")
                .add("net.oauth*")
                .add("org.apache.commons.codec*")
                .add("org.apache.log4j*")
                .add("org.bouncycastle*")
                .add("org.dom4j*")
                .add("org.eclipse.jetty.*")
                .add("org.jruby*")
                .add("org.json")
                .add("org.mozilla.javascript*")
                .add("org.slf4j*")
                .add("org.yaml*")
                .build());

        scannerConfig.setPackageVersions(ImmutableMap.<String, String>builder()
                .put("com.atlassian.activeobjects.spi*", getVersionFromMavenMetadata("com.atlassian.activeobjects", "activeobjects-spi", "0.19.7-remoteapps-1"))
                .put("com.atlassian.event.api*", getVersionFromMavenMetadata("com.atlassian.event", "atlassian-event", "2.2.0-m1"))
                .put("com.atlassian.plugin*", getVersionFromMavenMetadata("com.atlassian.plugins", "atlassian-plugins-core", "2.13.0-m2"))
                .put("com.atlassian.sal.api*", getVersionFromMavenMetadata("com.atlassian.sal", "sal-api", "2.7.0"))
                .put("com.atlassian.security.random*", getVersionFromMavenMetadata("com.atlassian.security", "atlassian-secure-random", "1.0"))
                .put("com.google.common.*", getVersionFromMavenMetadata("com.google.guava", "guava", "1"))
                .put("javax.servlet", "2.5")
                .put("javax.servlet.http", "2.5")
                .put("org.apache.commons.codec*", "1.3")
                .put("org.apache.commons.collections*", "3.2")
                .put("org.apache.commons.lang*", getVersionFromMavenMetadata("commons-lang", "commons-lang", "2.4"))
                .put("org.slf4j*", getVersionFromMavenMetadata("org.slf4j", "slf4j-api", "1.6.4"))
                .build());

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

        final Scanner scanner;
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
                try
                {
                    appFile = appFile.getCanonicalFile();
                }
                catch (IOException e)
                {
                    throw new RuntimeException("Unable to determine canonical path", e);
                }
                if (!appFile.exists())
                {
                    throw new FileNotFoundException("App '" + app + "' not found");
                }
                if (appFile.isDirectory())
                {
                    System.setProperty("plugin.resource.directories", appFile.getAbsolutePath());
                    descriptorAccessor = new PolygotDescriptorAccessor(appFile);
                    File appAsZip = zipAppDirectory(descriptorAccessor, appFile);
                    files.add(appAsZip);
                }
                else
                {
                    files.add(appFile);
                }
            }
            scanner = new FileListScanner(files);
        }

        final PluginLoader bundledPluginLoader = new BundledPluginLoader(this.getClass().getResource("/bundled-plugins.zip"), new File(".cache/bundled"), asList(osgiPluginDeployer, bundleFactory), pluginEventManager);
        final PluginLoader appPluginLoader = new ScanningPluginLoader(scanner, asList(osgiPluginDeployer, bundleFactory), pluginEventManager);

        final DefaultHostContainer hostContainer = new DefaultHostContainer();
        pluginManager = new DefaultPluginManager(
                new MemoryPluginPersistentStateStore(),
                asList(bundledPluginLoader, appPluginLoader),
                new DefaultModuleDescriptorFactory(hostContainer),
                pluginEventManager
        );

        final RemoteAppsPluginSettingsFactory pluginSettingsFactory = new RemoteAppsPluginSettingsFactory();

        hostComponents.put(PluginSettingsFactory.class, pluginSettingsFactory);
        hostComponents.put(ApplicationProperties.class, new RemoteAppsApplicationPropertiesServiceFactory(server));

        hostComponents.put(EventPublisher.class, new RemoteAppsEventPublisher());

        final EnvironmentFactory environmentFactory = new EnvironmentFactory(pluginSettingsFactory,
                pluginManager);
        final OAuthSignedRequestHandlerServiceFactory oAuthSignedRequestHandlerServiceFactory = new OAuthSignedRequestHandlerServiceFactory(environmentFactory, httpServer);
        final RequestContext requestContext = new DefaultRequestContext();
        final ContainerHttpResourceMounterServiceFactory containerHttpResourceMounterServiceFactory = new ContainerHttpResourceMounterServiceFactory(pluginManager, httpServer, oAuthSignedRequestHandlerServiceFactory, environmentFactory, requestContext);
        final RequestKiller requestKiller = new RequestKiller();
        final AsyncHttpClient asyncHttpClient = new DefaultAsyncHttpClient(requestKiller);
        final RequestContextServiceFactory requestContextServiceFactory
            = new RequestContextServiceFactory();
        final ContainerHostHttpClientServiceFactory containerHostHttpClientServiceFactory
            = new ContainerHostHttpClientServiceFactory(asyncHttpClient, requestContextServiceFactory,
            oAuthSignedRequestHandlerServiceFactory);
        final SyncHostHttpClientServiceFactory syncHostHttpClientServiceFactory
            = new SyncHostHttpClientServiceFactory(containerHostHttpClientServiceFactory);

        hostComponents.put(SignedRequestHandler.class, oAuthSignedRequestHandlerServiceFactory);
        hostComponents.put(HttpResourceMounter.class, containerHttpResourceMounterServiceFactory);
        hostComponents.put(PluginAccessor.class, pluginManager);
        hostComponents.put(PluginController.class, pluginManager);
        hostComponents.put(PluginEventManager.class, pluginEventManager);
        hostComponents.put(PluginSettingsAsyncFactory.class, new DefaultPluginSettingsAsyncFactory(pluginSettingsFactory));
        hostComponents.put(ModuleFactory.class, new PrefixDelegatingModuleFactory(ImmutableSet.of(new ClassPrefixModuleFactory(hostContainer), new BeanPrefixModuleFactory())));
        hostComponents.put(RequestContext.class, requestContext);
        hostComponents.put(AsyncHttpClient.class, asyncHttpClient);
        hostComponents.put(HostHttpClient.class, containerHostHttpClientServiceFactory);
        hostComponents.put(SyncHostHttpClient.class, syncHostHttpClientServiceFactory);

        hostComponents.put(DataSourceProvider.class, new RemoteAppsDataSourceProviderServiceFactory());
        hostComponents.put(TransactionTemplate.class, new NoOpTransactionTemplate());
    }

    private String getVersionFromMavenMetadata(String groupId, String artifactId, String defaultValue)
    {
        final String resource = new StringBuilder(128)
                .append("META-INF/maven/")
                .append(groupId)
                .append("/")
                .append(artifactId)
                .append("/pom.properties")
                .toString();

        final String version = new ResourcePropertiesLoader(resource).load().get("version");
        if (version == null)
        {
            log.warn("Could not read version from maven metadata for {}:{} will use default {}", new Object[]{groupId, artifactId, defaultValue});
            return defaultValue;
        }
        log.debug("Version found from maven metadata for {}:{} is {}", new Object[]{groupId, artifactId, version});
        return version;
    }

    private File zipAppDirectory(DescriptorAccessor descriptorAccessor, File appFile)
    {
        try
        {
            return ZipWriter.zipAppIntoPluginJar(descriptorAccessor, appFile);
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("Unable to zip up app: " + appFile.getPath(), e);
        }
    }

    public void start() throws IOException
    {
        pluginManager.init();
        Set<URI> foundHosts = findHostProducts();
        if (descriptorAccessor != null)
        {
            String appKey = descriptorAccessor.getDescriptor().getRootElement().attributeValue("key");
            appReloader = new AppReloader(descriptorAccessor, httpServer.getLocalMountBaseUrl(appKey), foundHosts);
        }
        else
        {
            for (URI host : foundHosts)
            {
                for (String appKey : httpServer.getContextNames())
                {
                    registerApp(host, appKey, httpServer.getLocalMountBaseUrl(appKey));
                }
            }
        }
    }

    private Set<URI> findHostProducts()
    {
        Set<URI> found = newHashSet();
        for (URI host : AUTOREGISTER_HOSTS)
        {
            Socket socket = null;
            try
            {
                log.debug("Scanning for host at " + host);
                socket = new Socket(host.getHost(), host.getPort());
                found.add(host);
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
        return found;
    }

    public void stop()
    {
        pluginManager.shutdown();
        if (appReloader != null)
        {
            appReloader.shutdown();
        }
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
