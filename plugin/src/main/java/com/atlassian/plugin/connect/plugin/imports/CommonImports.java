package com.atlassian.plugin.connect.plugin.imports;

import com.atlassian.applinks.spi.auth.AuthenticationConfigurationManager;
import com.atlassian.applinks.spi.link.MutatingApplicationLinkService;
import com.atlassian.applinks.spi.util.TypeAccessor;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.factory.HttpClientFactory;
import com.atlassian.jwt.applinks.JwtApplinkFinder;
import com.atlassian.jwt.applinks.JwtService;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.oauth.serviceprovider.ServiceProviderConsumerStore;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.servlet.ServletModuleManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.plugin.webresource.WebResourceUrlProvider;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.auth.AuthenticationController;
import com.atlassian.sal.api.auth.AuthenticationListener;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.message.LocaleResolver;
import com.atlassian.sal.api.net.RequestFactory;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.upm.api.license.RemotePluginLicenseService;
import com.atlassian.webhooks.spi.provider.ModuleDescriptorWebHookListenerRegistry;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * This class does nothing but is here to centralize the cross-product component imports.
 * This is so we have a single place to put the annotations instead of scattering them around the entire project
 */
@Named
public class CommonImports
{
    private final PluginController pluginController;
    private final TemplateRenderer templateRenderer;
    private final ServletModuleManager servletModuleManager;
    private final WebInterfaceManager webInterfaceManager;
    private final PluginRetrievalService pluginRetrievalService;
    private final ConsumerService consumerService;
    private final RequestFactory requestFactory;
    private final ApplicationProperties applicationProperties;
    private final ServiceProviderConsumerStore serviceProviderConsumerStore;
    private final MutatingApplicationLinkService applicationLinkService;
    private final AuthenticationConfigurationManager authenticationConfigurationManager;
    private final UserManager userManager;
    private final AuthenticationListener authenticationListener;
    private final AuthenticationController authenticationController;
    private final TransactionTemplate transactionTemplate;
    private final PluginEventManager pluginEventManager;
    private final PluginAccessor pluginAccessor;
    private final WebResourceManager webResourceManager;
    private final EventPublisher eventPublisher;
    private final I18nResolver i18nResolver;
    private final LocaleResolver localeResolver;
    private final PluginSettingsFactory pluginSettingsFactory;
    private final TypeAccessor typeAccessor;
    private final WebResourceUrlProvider webResourceUrlProvider;
    private final ModuleFactory moduleFactory;
    private final HttpClientFactory httpClientFactory;
    private final HttpClient httpClient;
    private final ModuleDescriptorWebHookListenerRegistry webHookListenerRegistry;
    private final RemotePluginLicenseService remotePluginLicenseService;
    private final JwtService jwtService;
    private final JwtApplinkFinder jwtApplinkFinder;


    @Inject
    public CommonImports(
            @ComponentImport PluginController pluginController,
            @ComponentImport TemplateRenderer templateRenderer,
            @ComponentImport ServletModuleManager servletModuleManager,
            @ComponentImport WebInterfaceManager webInterfaceManager,
            @ComponentImport PluginRetrievalService pluginRetrievalService,
            @ComponentImport ConsumerService consumerService,
            @ComponentImport RequestFactory requestFactory,
            @ComponentImport ApplicationProperties applicationProperties,
            @ComponentImport ServiceProviderConsumerStore serviceProviderConsumerStore,
            @ComponentImport MutatingApplicationLinkService applicationLinkService,
            @ComponentImport AuthenticationConfigurationManager authenticationConfigurationManager,
            @ComponentImport UserManager userManager,
            @ComponentImport AuthenticationListener authenticationListener,
            @ComponentImport AuthenticationController authenticationController,
            @ComponentImport TransactionTemplate transactionTemplate,
            @ComponentImport PluginEventManager pluginEventManager,
            @ComponentImport PluginAccessor pluginAccessor,
            @ComponentImport WebResourceManager webResourceManager,
            @ComponentImport EventPublisher eventPublisher,
            @ComponentImport I18nResolver i18nResolver,
            @ComponentImport LocaleResolver localeResolver,
            @ComponentImport PluginSettingsFactory pluginSettingsFactory,
            @ComponentImport TypeAccessor typeAccessor,
            @ComponentImport WebResourceUrlProvider webResourceUrlProvider,
            @ComponentImport ModuleFactory moduleFactory,
            @ComponentImport HttpClientFactory httpClientFactory,
            @ComponentImport HttpClient httpClient,
            @ComponentImport ModuleDescriptorWebHookListenerRegistry webHookListenerRegistry,
            @ComponentImport RemotePluginLicenseService remotePluginLicenseService,
            @ComponentImport JwtService jwtService,
            @ComponentImport JwtApplinkFinder jwtApplinkFinder)
    {
        this.pluginController = pluginController;
        this.templateRenderer = templateRenderer;
        this.servletModuleManager = servletModuleManager;
        this.webInterfaceManager = webInterfaceManager;
        this.pluginRetrievalService = pluginRetrievalService;
        this.consumerService = consumerService;
        this.requestFactory = requestFactory;
        this.applicationProperties = applicationProperties;
        this.serviceProviderConsumerStore = serviceProviderConsumerStore;
        this.applicationLinkService = applicationLinkService;
        this.authenticationConfigurationManager = authenticationConfigurationManager;
        this.userManager = userManager;
        this.authenticationListener = authenticationListener;
        this.authenticationController = authenticationController;
        this.transactionTemplate = transactionTemplate;
        this.pluginEventManager = pluginEventManager;
        this.pluginAccessor = pluginAccessor;
        this.webResourceManager = webResourceManager;
        this.eventPublisher = eventPublisher;
        this.i18nResolver = i18nResolver;
        this.localeResolver = localeResolver;
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.typeAccessor = typeAccessor;
        this.webResourceUrlProvider = webResourceUrlProvider;
        this.moduleFactory = moduleFactory;
        this.httpClientFactory = httpClientFactory;
        this.httpClient = httpClient;
        this.webHookListenerRegistry = webHookListenerRegistry;
        this.remotePluginLicenseService = remotePluginLicenseService;
        this.jwtService = jwtService;
        this.jwtApplinkFinder = jwtApplinkFinder;
    }
}
