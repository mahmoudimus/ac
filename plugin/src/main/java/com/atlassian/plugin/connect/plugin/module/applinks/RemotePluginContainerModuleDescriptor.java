package com.atlassian.plugin.connect.plugin.module.applinks;

import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.List;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.TypeNotInstalledException;
import com.atlassian.applinks.spi.application.ApplicationIdUtil;
import com.atlassian.applinks.spi.link.ApplicationLinkDetails;
import com.atlassian.applinks.spi.link.MutatingApplicationLinkService;
import com.atlassian.applinks.spi.util.TypeAccessor;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.ServiceProvider;
import com.atlassian.oauth.serviceprovider.ServiceProviderConsumerStore;
import com.atlassian.oauth.util.RSAKeys;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginInformation;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.spi.ConnectAddOnIdentifierService;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.descriptors.CannotDisable;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.connect.plugin.util.BundleUtil;
import com.atlassian.plugin.connect.plugin.OAuthLinkManager;
import com.atlassian.plugin.connect.plugin.PermissionManager;
import com.atlassian.plugin.connect.plugin.util.OsgiServiceUtils;
import com.atlassian.plugin.connect.spi.Permissions;
import com.atlassian.plugin.connect.spi.applinks.RemotePluginContainerApplicationType;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.util.concurrent.NotNull;

import org.dom4j.Element;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.*;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Dynamically creates an application link for a plugin host
 */
@CannotDisable
public final class RemotePluginContainerModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    public static final String PLUGIN_KEY_PROPERTY = "plugin-key";

    private final MutatingApplicationLinkService applicationLinkService;
    private final OAuthLinkManager oAuthLinkManager;
    private final PermissionManager permissionManager;
    private final TypeAccessor typeAccessor;
    private final BundleContext bundleContext;
    private final PluginSettingsFactory pluginSettingsFactory;
    private final ConnectAddOnIdentifierService connectIdentifier;

    private static final Logger log = LoggerFactory.getLogger(RemotePluginContainerModuleDescriptor.class);

    private URI displayUrl;
    private Element oauthElement;
    private ApplicationLinkDetails applicationLinkDetails;
    private boolean remoteMode;
    private Bundle pluginBundle;

    public RemotePluginContainerModuleDescriptor(
            MutatingApplicationLinkService applicationLinkService,
            OAuthLinkManager oAuthLinkManager,
            PermissionManager permissionManager,
            TypeAccessor typeAccessor,
            BundleContext bundleContext,
            PluginSettingsFactory pluginSettingsFactory,
            ConnectAddOnIdentifierService connectIdentifier)
    {
        super(ModuleFactory.LEGACY_MODULE_FACTORY);
        this.applicationLinkService = checkNotNull(applicationLinkService);
        this.oAuthLinkManager = checkNotNull(oAuthLinkManager);
        this.permissionManager = checkNotNull(permissionManager);
        this.typeAccessor = checkNotNull(typeAccessor);
        this.bundleContext = checkNotNull(bundleContext);
        this.pluginSettingsFactory = checkNotNull(pluginSettingsFactory);
        this.connectIdentifier = checkNotNull(connectIdentifier);
    }

    @Override
    public void init(@NotNull Plugin plugin, @NotNull Element element) throws PluginParseException
    {
        super.init(plugin, element);
        this.oauthElement = element.element("oauth");
        this.displayUrl = getRequiredUriAttribute(element, "display-url");
        this.applicationLinkDetails = ApplicationLinkDetails.builder()
                .displayUrl(displayUrl)
                .isPrimary(false)
                // todo: support i18n names
                .name(plugin.getName() != null ? plugin.getName() : plugin.getKey())
                .rpcUrl(displayUrl)
                .build();

        if (null != element.getParent() && element.getParent().elements(element.getName()).size() > 1)
        {
            throw new PluginParseException("Can only have one remote-plugin-container module in a descriptor");
        }
        this.remoteMode = connectIdentifier.isConnectAddOn(BundleUtil.findBundleForPlugin(bundleContext, plugin.getKey()));
    }

    @Override
    public void enabled()
    {
        this.pluginBundle = BundleUtil.findBundleForPlugin(bundleContext, getPluginKey());

        if (remoteMode)
        {
            final ApplicationId expectedApplicationId = ApplicationIdUtil.generate(displayUrl);
            ApplicationLink link = null;
            final RemotePluginContainerApplicationType applicationType = typeAccessor.getApplicationType(
                    RemotePluginContainerApplicationType.class);
            try
            {
                link = applicationLinkService.getApplicationLink(expectedApplicationId);
            }
            catch (TypeNotInstalledException ex)
            {
                log.info("Link found for '{}' but the type cannot be found, treating as not found", getPluginKey());
                manuallyDeleteApplicationId(expectedApplicationId);
            }

// @todo this should work but it just fucks stuff up beyond belief (instead of lines 160-170)
//            try to find link with old display url
//            for (ApplicationLink otherLink : applicationLinkService.getApplicationLinks(RemotePluginContainerApplicationType.class))
//            {
//                if (getPluginKey().equals(otherLink.getProperty(PLUGIN_KEY_PROPERTY)) && (link == null || link.getId().get().equals(otherLink.getId().get())))
//                {
//                    log.debug("Old application link for this plugin '{}' found with different display url '{}', removing",
//                        getPluginKey(), displayUrl);
//                    applicationLinkService.deleteApplicationLink(otherLink);
//                }
//            }

            if (link != null)
            {
                if (getPluginKey().equals(link.getProperty(PLUGIN_KEY_PROPERTY)))
                {
                    log.info("Application link for remote plugin container '{}' already exists", getPluginKey());
                }
// @todo this should work but it just fucks stuff up beyond belief (instead of lines 160-170)
//                else if (link.getProperty(PLUGIN_KEY_PROPERTY) == null)
//                {
//                    log.warn("Found application link for url '{}' is missing associated plugin key", displayUrl);
//                    link.putProperty(PLUGIN_KEY_PROPERTY, getPluginKey());
//                }
                else
                {
                    throw new PluginParseException("Application link already exists for id '" + expectedApplicationId + "' but it isn't the target " +
                            " plugin '" + getPluginKey() + "': unexpected plugin key is: " + link.getProperty(PLUGIN_KEY_PROPERTY));
                }
            }
            else
            {
                // try to find link with old display url
                for (ApplicationLink otherLink : applicationLinkService.getApplicationLinks(RemotePluginContainerApplicationType.class))
                {
                    if (getPluginKey().equals(otherLink.getProperty(PLUGIN_KEY_PROPERTY)))
                    {
                        log.debug("Old application link for this plugin '{}' found with different display url '{}', removing",
                            getPluginKey(), displayUrl);
                        applicationLinkService.deleteApplicationLink(otherLink);
                    }
                }

                log.info("Creating an application link for the remote plugin container of key '{}'", getPluginKey());
                link = applicationLinkService.addApplicationLink(expectedApplicationId, applicationType, applicationLinkDetails);
                link.putProperty(PLUGIN_KEY_PROPERTY, getPluginKey());
            }

            link.putProperty("IS_ACTIVITY_ITEM_PROVIDER", Boolean.FALSE.toString());
            link.putProperty("system", Boolean.TRUE.toString());

            ServiceProvider serviceProvider = createOAuthServiceProvider(displayUrl, oauthElement);
            oAuthLinkManager.associateProviderWithLink(link, applicationType.getId().get(), serviceProvider);

            if (oauthElement != null)
            {
                registerOAuth(link, oauthElement);
            }
        }
        else
        {
            log.info("Plugin '{}' in local mode, so not setting up remote plugin container link", getPluginKey());
        }
        super.enabled();
    }

    private void manuallyDeleteApplicationId(ApplicationId expectedApplicationId)
    {
        PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
        List<String> applicationIds = (List<String>) pluginSettings.get("applinks.global.application.ids");
        if (applicationIds != null)
        {
            if (applicationIds.remove(expectedApplicationId.get()))
            {
                pluginSettings.put("applinks.global.application.ids", applicationIds);
            }
            else
            {
                throw new IllegalStateException("Cannot find application id " + expectedApplicationId.get() + " to delete");
            }
        }
        else
        {
            throw new IllegalStateException("Cannot find application ids to manually delete " + expectedApplicationId.get());
        }
    }

    @Override
    public void disabled()
    {
        super.disabled();
        if (remoteMode && pluginBundle != null)
        {
            // we have to retrive services fresh from plugin bundle as it is possible this is called after the
            // remotable plugins plugin has been disabled
            MutatingApplicationLinkService applicationLinkService = getService(MutatingApplicationLinkService.class);

            for (ApplicationLink link : applicationLinkService.getApplicationLinks())
            {
                if (displayUrl.equals(link.getRpcUrl()))
                {
                    log.info("Removing application link for display url '{}'", displayUrl);
                    applicationLinkService.deleteApplicationLink(link);
                }
            }

            // this repeats logic in {@link OAuthLinkManager#unassociateConsumer()} because, again, all services
            // need to be retrieved fresh
            ServiceProviderConsumerStore store = getService(ServiceProviderConsumerStore.class);
            if (store.get(getPluginKey()) != null)
            {
                store.remove(getPluginKey());
            }
        }
    }

    private ServiceProvider createOAuthServiceProvider(URI displayUrl, Element oauthElement)
    {
        if (oauthElement != null)
        {
            final URI requestTokenUrl = URI.create(displayUrl + getOptionalAttribute(oauthElement, "request-token-url", "/request-token"));
            final URI accessTokenUrl = URI.create(displayUrl + getOptionalAttribute(oauthElement, "access-token-url", "/access-token"));
            final URI authorizeUrl = URI.create(displayUrl + getOptionalAttribute(oauthElement, "authorize-url", "/authorize"));
            return new ServiceProvider(requestTokenUrl, accessTokenUrl, authorizeUrl);
        }
        else
        {
            // set up the link with a dummy so that outgoing links get signed even if no oauth element
            // is defined
            URI dummyUri = URI.create("http://localhost");
            return new ServiceProvider(dummyUri, dummyUri, dummyUri);
        }
    }

    private void registerOAuth(ApplicationLink link, Element oauthElement)
    {
        permissionManager.requirePermission(getPluginKey(), Permissions.CREATE_OAUTH_LINK);

        final PluginInformation pluginInfo = getPlugin().getPluginInformation();
        final String name = getPlugin().getName();
        final String description = pluginInfo.getDescription();
        final URI callback = URI.create(displayUrl + getOptionalAttribute(oauthElement, "callback", "/callback"));
        final PublicKey publicKey = getPublicKey(getRequiredElementText(oauthElement, "public-key"));

        Consumer consumer = Consumer.key(getPluginKey()).name(name != null ? name : getPluginKey()).publicKey(publicKey).description(description).callback(
                        callback).build();

        oAuthLinkManager.associateConsumerWithLink(link, consumer);

        // provider is already configured as part of the applink creation
    }

    protected final PublicKey getPublicKey(String publicKeyText)
    {
        PublicKey publicKey;
        try
        {
            if (publicKeyText.startsWith("-----BEGIN CERTIFICATE-----"))
            {
                publicKey = RSAKeys.fromEncodedCertificateToPublicKey(publicKeyText);
            }
            else
            {
                publicKey = RSAKeys.fromPemEncodingToPublicKey(publicKeyText);
            }
        }
        catch (GeneralSecurityException e)
        {
            throw new PluginParseException("Invalid public key", e);
        }
        return publicKey;
    }

    private <T> T getService(Class<T> interfaceClass)
    {
        if (pluginBundle != null)
        {
            return OsgiServiceUtils.getService(pluginBundle.getBundleContext(), interfaceClass);
        }
        else
        {
            throw new IllegalStateException("Cannot retrieve services from unknown plugin bundle: " + getCompleteKey());
        }
    }

    @Override
    public Void getModule()
    {
        return null;
    }
}
