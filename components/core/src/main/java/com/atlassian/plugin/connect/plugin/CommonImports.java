package com.atlassian.plugin.connect.plugin;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.applinks.spi.auth.AuthenticationConfigurationManager;
import com.atlassian.applinks.spi.link.MutatingApplicationLinkService;
import com.atlassian.applinks.spi.util.TypeAccessor;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.httpclient.api.factory.HttpClientFactory;
import com.atlassian.jwt.JwtService;
import com.atlassian.jwt.applinks.JwtApplinkFinder;
import com.atlassian.jwt.writer.JwtJsonBuilderFactory;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.oauth.serviceprovider.ServiceProviderConsumerStore;
import com.atlassian.oauth.serviceprovider.ServiceProviderTokenStore;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.servlet.ServletModuleManager;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.plugin.webresource.WebResourceUrlProvider;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.auth.AuthenticationController;
import com.atlassian.sal.api.auth.AuthenticationListener;
import com.atlassian.sal.api.features.DarkFeatureManager;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.message.LocaleResolver;
import com.atlassian.sal.api.net.RequestFactory;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.upm.api.license.RemotePluginLicenseService;
import com.atlassian.webhooks.spi.provider.ModuleDescriptorWebHookListenerRegistry;
import com.atlassian.webresource.api.assembler.PageBuilderService;

import javax.inject.Inject;

/**
 * This class does nothing but is here to centralize the cross-product component imports.
 * This is so we have a single place to put the annotations instead of scattering them around the entire project
 */
@SuppressWarnings ("ALL")
@Scanned
public class CommonImports
{
    @Inject
    public CommonImports(
            @ComponentImport PluginController pluginController,
            @ComponentImport TemplateRenderer templateRenderer,
            @ComponentImport SoyTemplateRenderer soyTemplateRenderer,
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
            @ComponentImport ModuleDescriptorWebHookListenerRegistry webHookListenerRegistry,
            @ComponentImport RemotePluginLicenseService remotePluginLicenseService,
            @ComponentImport JwtJsonBuilderFactory jwtBuilderFactory,
            @ComponentImport JwtService jwtService,
            @ComponentImport JwtApplinkFinder jwtApplinkFinder,
            @ComponentImport ServiceProviderTokenStore serviceProviderTokenStore,
            @ComponentImport CrowdService crowdService,
            @ComponentImport DarkFeatureManager darkFeatureManager,
            @ComponentImport ActiveObjects activeObjects,
            @ComponentImport PageBuilderService pageBuilderService)
    {
    }
}
