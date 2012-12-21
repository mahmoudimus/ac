package com.atlassian.plugin.remotable.container;

import com.atlassian.activeobjects.spi.DataSourceProvider;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.factories.PluginFactory;
import com.atlassian.plugin.remotable.api.service.jira.JiraComponentClient;
import com.atlassian.plugin.remotable.api.service.jira.JiraIssueClient;
import com.atlassian.plugin.remotable.api.service.jira.JiraMetadataClient;
import com.atlassian.plugin.remotable.api.service.jira.JiraProjectClient;
import com.atlassian.plugin.remotable.api.service.jira.JiraSearchClient;
import com.atlassian.plugin.remotable.api.service.jira.JiraUserClient;
import com.atlassian.plugin.remotable.api.service.jira.JiraVersionClient;
import com.atlassian.plugin.DefaultModuleDescriptorFactory;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.event.impl.DefaultPluginEventManager;
import com.atlassian.plugin.hostcontainer.DefaultHostContainer;
import com.atlassian.plugin.loaders.BundledPluginLoader;
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
import com.atlassian.plugin.remotable.api.service.EmailSender;
import com.atlassian.plugin.remotable.api.service.HttpResourceMounter;
import com.atlassian.plugin.remotable.api.service.RenderContext;
import com.atlassian.plugin.remotable.api.service.RequestContext;
import com.atlassian.plugin.remotable.api.service.SignedRequestHandler;
import com.atlassian.plugin.remotable.api.service.confluence.ConfluenceAdminClient;
import com.atlassian.plugin.remotable.api.service.confluence.ConfluenceAttachmentClient;
import com.atlassian.plugin.remotable.api.service.confluence.ConfluenceBlogClient;
import com.atlassian.plugin.remotable.api.service.confluence.ConfluenceLabelClient;
import com.atlassian.plugin.remotable.api.service.confluence.ConfluenceNotificationClient;
import com.atlassian.plugin.remotable.api.service.confluence.ConfluencePageClient;
import com.atlassian.plugin.remotable.api.service.confluence.ConfluenceSpaceClient;
import com.atlassian.plugin.remotable.api.service.confluence.ConfluenceUserClient;
import com.atlassian.plugin.remotable.api.service.http.HostHttpClient;
import com.atlassian.plugin.remotable.api.service.http.HostXmlRpcClient;
import com.atlassian.plugin.remotable.container.ao.ContainerDataSourceProvider;
import com.atlassian.plugin.remotable.container.internal.EnvironmentFactory;
import com.atlassian.plugin.remotable.container.service.ContainerEmailSender;
import com.atlassian.plugin.remotable.container.service.ContainerHttpResourceMounterServiceFactory;
import com.atlassian.plugin.remotable.container.service.OAuthSignedRequestHandlerServiceFactory;
import com.atlassian.plugin.remotable.container.service.event.ContainerEventPublisher;
import com.atlassian.plugin.remotable.container.service.plugins.NoOpWebResourceManager;
import com.atlassian.plugin.remotable.container.service.sal.*;
import com.atlassian.plugin.remotable.container.util.ZipWriter;
import com.atlassian.plugin.remotable.host.common.HostProperties;
import com.atlassian.plugin.remotable.host.common.descriptor.DescriptorAccessor;
import com.atlassian.plugin.remotable.host.common.descriptor.DescriptorPermissionsReader;
import com.atlassian.plugin.remotable.host.common.descriptor.PolyglotDescriptorAccessor;
import com.atlassian.plugin.remotable.host.common.service.RenderContextServiceFactory;
import com.atlassian.plugin.remotable.host.common.service.RequestContextServiceFactory;
import com.atlassian.plugin.remotable.host.common.service.confluence.ConfluenceAdminClientServiceFactory;
import com.atlassian.plugin.remotable.host.common.service.confluence.ConfluenceAttachmentClientServiceFactory;
import com.atlassian.plugin.remotable.host.common.service.confluence.ConfluenceBlogClientServiceFactory;
import com.atlassian.plugin.remotable.host.common.service.confluence.ConfluenceLabelClientServiceFactory;
import com.atlassian.plugin.remotable.host.common.service.confluence.ConfluenceNotificationClientServiceFactory;
import com.atlassian.plugin.remotable.host.common.service.confluence.ConfluencePageClientServiceFactory;
import com.atlassian.plugin.remotable.host.common.service.confluence.ConfluenceSpaceClientServiceFactory;
import com.atlassian.plugin.remotable.host.common.service.confluence.ConfluenceUserClientServiceFactory;
import com.atlassian.plugin.remotable.host.common.service.http.HostHttpClientConsumerServiceFactory;
import com.atlassian.plugin.remotable.host.common.service.http.HostHttpClientServiceFactory;
import com.atlassian.plugin.remotable.host.common.service.http.HostXmlRpcClientServiceFactory;
import com.atlassian.plugin.remotable.host.common.service.jira.JiraComponentClientServiceFactory;
import com.atlassian.plugin.remotable.host.common.service.jira.JiraIssueClientServiceFactory;
import com.atlassian.plugin.remotable.host.common.service.jira.JiraMetadataClientServiceFactory;
import com.atlassian.plugin.remotable.host.common.service.jira.JiraProjectClientServiceFactory;
import com.atlassian.plugin.remotable.host.common.service.jira.JiraSearchClientServiceFactory;
import com.atlassian.plugin.remotable.host.common.service.jira.JiraUserClientServiceFactory;
import com.atlassian.plugin.remotable.host.common.service.jira.JiraVersionClientServiceFactory;
import com.atlassian.plugin.remotable.host.common.util.BundleLocator;
import com.atlassian.plugin.remotable.host.common.util.BundleUtil;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.message.LocaleResolver;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.core.transaction.NoOpTransactionTemplate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.atlassian.plugin.remotable.container.util.AppRegister.*;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.*;
import static com.google.common.collect.Sets.*;
import static java.util.Arrays.asList;

public final class Container
{
    private static final Logger log = LoggerFactory.getLogger(Container.class);

    public static final Set<URI> AUTOREGISTER_HOSTS = ImmutableSet.of(
            URI.create("http://localhost:1990/confluence"),
            URI.create("http://localhost:2990/jira"),
            URI.create("http://localhost:5990/refapp"));

    private final ContainerConfiguration configuration;
    private final HttpServer httpServer;
    private final DefaultPluginManager pluginManager;

    private DescriptorAccessor descriptorAccessor;
    private AppReloader appReloader;

    public Container(ContainerConfiguration configuration, HttpServer server) throws FileNotFoundException
    {
        this.configuration = checkNotNull(configuration);
        this.httpServer = checkNotNull(server);

        // todo: this should use the plugin api, but it doesn't allow setting of plugin loaders right now
        final DefaultPackageScannerConfiguration scannerConfig = new DefaultPackageScannerConfiguration(ContainerApplication.INSTANCE.getVersion());
        OsgiPersistentCache osgiCache = new DefaultOsgiPersistentCache(configuration.getCacheDirectory("osgi"));
        Map<Class<?>, Object> hostComponents = newHashMap();
        PluginEventManager pluginEventManager = new DefaultPluginEventManager();
        final OsgiContainerManager osgiContainerManager = new FelixOsgiContainerManager(
                osgiCache,
                scannerConfig,
                new ContainerHostComponentProvider(hostComponents),
                pluginEventManager);

        OsgiBundleFactory bundleFactory = new OsgiBundleFactory(osgiContainerManager, pluginEventManager);
        OsgiPluginFactory osgiPluginDeployer = new OsgiPluginFactory(
                PluginAccessor.Descriptor.FILENAME,
                ImmutableSet.of(ContainerApplication.INSTANCE),
                osgiCache,
                osgiContainerManager,
                pluginEventManager);

        final Scanner scanner;
        if (Iterables.isEmpty(configuration.getApplicationsPaths()))
        {
            File appDir = ContainerUtils.mkdirs("apps");
            scanner = new DirectoryScanner(appDir);
        }
        else
        {
            List<File> files = newArrayList();
            for (String app : configuration.getApplicationsPaths())
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
                    descriptorAccessor = new PolyglotDescriptorAccessor(appFile);
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

        final List<PluginFactory> pluginFactories = Arrays.<PluginFactory>asList(osgiPluginDeployer, bundleFactory);
        final PluginLoader bundledPluginLoader = new BundledPluginLoader(this.getClass().getResource("/bundled-plugins.zip"), configuration.getCacheDirectory("bundled"), pluginFactories, pluginEventManager);
        final PluginLoader appPluginLoader = new ScanningPluginLoader(scanner, pluginFactories, pluginEventManager);

        final DefaultHostContainer hostContainer = new DefaultHostContainer();
        pluginManager = new DefaultPluginManager(
                new MemoryPluginPersistentStateStore(),
                asList(bundledPluginLoader, appPluginLoader),
                new DefaultModuleDescriptorFactory(hostContainer),
                pluginEventManager
        );

        ContainerDataSourceProvider dataSourceProvider = new ContainerDataSourceProvider(
                new ContainerApplicationProperties(null).getHomeDirectory());
        hostComponents.put(DataSourceProvider.class, dataSourceProvider);

        final JdbcPluginSettingsFactory pluginSettingsFactory = new JdbcPluginSettingsFactory(dataSourceProvider);
        hostComponents.put(PluginSettingsFactory.class, pluginSettingsFactory);
        final ContainerApplicationPropertiesServiceFactory applicationPropertiesServiceFactory = new ContainerApplicationPropertiesServiceFactory(server);
        hostComponents.put(ApplicationProperties.class, applicationPropertiesServiceFactory);

        hostComponents.put(EventPublisher.class, new ContainerEventPublisher());

        final EnvironmentFactory environmentFactory = new EnvironmentFactory(pluginSettingsFactory, pluginManager);
        final OAuthSignedRequestHandlerServiceFactory oAuthSignedRequestHandlerServiceFactory = new OAuthSignedRequestHandlerServiceFactory(environmentFactory, httpServer);
        final RequestContextServiceFactory requestContextServiceFactory = new RequestContextServiceFactory(oAuthSignedRequestHandlerServiceFactory);
        final ContainerHttpResourceMounterServiceFactory containerHttpResourceMounterServiceFactory = new ContainerHttpResourceMounterServiceFactory(pluginManager, httpServer, oAuthSignedRequestHandlerServiceFactory, environmentFactory, requestContextServiceFactory);
        final HostHttpClientServiceFactory hostHttpClientServiceFactory = new HostHttpClientServiceFactory(requestContextServiceFactory, oAuthSignedRequestHandlerServiceFactory);
        final HostXmlRpcClientServiceFactory hostXmlRpcClientHostServiceFactory = new HostXmlRpcClientServiceFactory(hostHttpClientServiceFactory);
        final ContainerLocaleResolver localeResolver = new ContainerLocaleResolver();
        final ContainerI18nResolver i18nResolver = new ContainerI18nResolver(pluginManager, pluginEventManager, new ResourceBundleResolverImpl());
        final RenderContextServiceFactory renderContextServiceFactory = new RenderContextServiceFactory(requestContextServiceFactory, oAuthSignedRequestHandlerServiceFactory, localeResolver, i18nResolver);

        hostComponents.put(SignedRequestHandler.class, oAuthSignedRequestHandlerServiceFactory);
        hostComponents.put(HttpResourceMounter.class, containerHttpResourceMounterServiceFactory);
        hostComponents.put(PluginAccessor.class, pluginManager);
        hostComponents.put(PluginController.class, pluginManager);
        hostComponents.put(PluginEventManager.class, pluginEventManager);
        hostComponents.put(ModuleFactory.class, new PrefixDelegatingModuleFactory(ImmutableSet.of(new ClassPrefixModuleFactory(hostContainer), new BeanPrefixModuleFactory())));
        hostComponents.put(RequestContext.class, requestContextServiceFactory);
        hostComponents.put(HostHttpClient.class, hostHttpClientServiceFactory);
        hostComponents.put(HostXmlRpcClient.class, hostXmlRpcClientHostServiceFactory);
        hostComponents.put(EmailSender.class, new HostHttpClientConsumerServiceFactory<EmailSender>(hostHttpClientServiceFactory, ContainerEmailSender.class));
        hostComponents.put(LocaleResolver.class, localeResolver);
        hostComponents.put(I18nResolver.class, i18nResolver);
        hostComponents.put(WebResourceManager.class, new NoOpWebResourceManager());
        hostComponents.put(RenderContext.class, renderContextServiceFactory);

        hostComponents.put(TransactionTemplate.class, new NoOpTransactionTemplate());
        hostComponents.put(UserManager.class, new ContainerUserManagerServiceFactory(requestContextServiceFactory));

        // jira services
        hostComponents.put(JiraComponentClient.class, new JiraComponentClientServiceFactory(hostHttpClientServiceFactory));
        hostComponents.put(JiraProjectClient.class, new JiraProjectClientServiceFactory(hostHttpClientServiceFactory));
        hostComponents.put(JiraVersionClient.class, new JiraVersionClientServiceFactory(hostHttpClientServiceFactory));
        JiraMetadataClientServiceFactory metadataFactory = new JiraMetadataClientServiceFactory(hostHttpClientServiceFactory);
        hostComponents.put(JiraMetadataClient.class, metadataFactory);
        hostComponents.put(JiraIssueClient.class, new JiraIssueClientServiceFactory(hostHttpClientServiceFactory, requestContextServiceFactory, metadataFactory));
        hostComponents.put(JiraSearchClient.class, new JiraSearchClientServiceFactory(hostHttpClientServiceFactory));
        hostComponents.put(JiraUserClient.class, new JiraUserClientServiceFactory(hostHttpClientServiceFactory));

        DescriptorPermissionsReader permissionsReader = createPermissionsReaderForProduct(osgiContainerManager, "confluence");

        // confluence services
        hostComponents.put(ConfluenceSpaceClient.class, new ConfluenceSpaceClientServiceFactory(hostHttpClientServiceFactory, hostXmlRpcClientHostServiceFactory,
                permissionsReader, pluginManager, requestContextServiceFactory));
        hostComponents.put(ConfluencePageClient.class, new ConfluencePageClientServiceFactory(hostHttpClientServiceFactory, hostXmlRpcClientHostServiceFactory,
                        permissionsReader, pluginManager, requestContextServiceFactory));
        hostComponents.put(ConfluenceAdminClient.class, new ConfluenceAdminClientServiceFactory(hostHttpClientServiceFactory, hostXmlRpcClientHostServiceFactory,
                        permissionsReader, pluginManager, requestContextServiceFactory));
        hostComponents.put(ConfluenceBlogClient.class, new ConfluenceBlogClientServiceFactory(hostHttpClientServiceFactory, hostXmlRpcClientHostServiceFactory,
                        permissionsReader, pluginManager, requestContextServiceFactory));
        hostComponents.put(ConfluenceLabelClient.class, new ConfluenceLabelClientServiceFactory(hostHttpClientServiceFactory, hostXmlRpcClientHostServiceFactory,
                        permissionsReader, pluginManager, requestContextServiceFactory));
        hostComponents.put(ConfluenceNotificationClient.class, new ConfluenceNotificationClientServiceFactory(hostHttpClientServiceFactory, hostXmlRpcClientHostServiceFactory,
                        permissionsReader, pluginManager, requestContextServiceFactory));
        hostComponents.put(ConfluenceUserClient.class, new ConfluenceUserClientServiceFactory(hostHttpClientServiceFactory, hostXmlRpcClientHostServiceFactory,
                        permissionsReader, pluginManager, requestContextServiceFactory));
        hostComponents.put(ConfluenceAttachmentClient.class, new ConfluenceAttachmentClientServiceFactory(hostHttpClientServiceFactory, hostXmlRpcClientHostServiceFactory,
                        permissionsReader, pluginManager, requestContextServiceFactory));
    }

    private DescriptorPermissionsReader createPermissionsReaderForProduct(final OsgiContainerManager osgiContainerManager, final String productKey)
    {
        return new DescriptorPermissionsReader(new HostProperties()
        {
            @Override
            public String getKey()
            {
                return productKey;
            }
        }, new BundleLocator()
        {
            @Override
            public Bundle getBundle(String pluginKey)
            {
                return BundleUtil.findBundleForPlugin(osgiContainerManager.getBundles()[0].getBundleContext(), pluginKey);
            }
        });
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

    public void start() throws Exception
    {
        pluginManager.init();
        httpServer.start();
        Set<URI> foundHosts = findHostProducts();
        if (descriptorAccessor != null)
        {
            final String appKey = descriptorAccessor.getKey();
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
