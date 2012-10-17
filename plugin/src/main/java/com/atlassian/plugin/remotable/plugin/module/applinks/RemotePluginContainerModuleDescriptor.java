package com.atlassian.plugin.remotable.plugin.module.applinks;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.TypeNotInstalledException;
import com.atlassian.applinks.spi.application.ApplicationIdUtil;
import com.atlassian.applinks.spi.link.ApplicationLinkDetails;
import com.atlassian.applinks.spi.link.MutatingApplicationLinkService;
import com.atlassian.applinks.spi.util.TypeAccessor;
import com.atlassian.plugin.remotable.plugin.OAuthLinkManager;
import com.atlassian.plugin.remotable.plugin.PermissionManager;
import com.atlassian.plugin.remotable.spi.Permissions;
import com.atlassian.plugin.remotable.spi.applinks.RemotePluginContainerApplicationType;
import com.atlassian.plugin.remotable.host.common.util.BundleUtil;
import com.atlassian.plugin.remotable.plugin.util.RemotePluginUtil;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.ServiceProvider;
import com.atlassian.oauth.util.RSAKeys;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginInformation;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.descriptors.CannotDisable;
import com.atlassian.util.concurrent.NotNull;
import org.dom4j.Element;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.PublicKey;

import static com.atlassian.plugin.remotable.spi.util.Dom4jUtils.*;

/**
 * Dynamically creates an application link for a plugin host
 */
@CannotDisable
public class RemotePluginContainerModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    public static final String PLUGIN_KEY_PROPERTY = "plugin-key";

    private final MutatingApplicationLinkService applicationLinkService;
    private final OAuthLinkManager oAuthLinkManager;
    private final PermissionManager permissionManager;
    private final TypeAccessor typeAccessor;
    private final BundleContext bundleContext;

    private static final Logger log = LoggerFactory.getLogger(RemotePluginContainerModuleDescriptor.class);

    private URI displayUrl;
    private Element oauthElement;
    private ApplicationLinkDetails applicationLinkDetails;
    private boolean remoteMode;

    public RemotePluginContainerModuleDescriptor(MutatingApplicationLinkService applicationLinkService,
                                                 OAuthLinkManager oAuthLinkManager,
                                                 PermissionManager permissionManager,
                                                 TypeAccessor typeAccessor,
                                                 BundleContext bundleContext
    )
    {
        this.applicationLinkService = applicationLinkService;
        this.oAuthLinkManager = oAuthLinkManager;
        this.permissionManager = permissionManager;
        this.typeAccessor = typeAccessor;
        this.bundleContext = bundleContext;
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

        if (element.getParent().elements(element.getName()).size() > 1)
        {
            throw new PluginParseException("Can only have one remote-plugin-container module in a descriptor");
        }
        this.remoteMode = RemotePluginUtil.isRemoteMode(BundleUtil.findBundleForPlugin(bundleContext, plugin.getKey()));
    }

    @Override
    public void enabled()
    {
        if (remoteMode)
        {
            final ApplicationId expectedApplicationId = ApplicationIdUtil.generate(displayUrl);
            ApplicationLink link;
            final RemotePluginContainerApplicationType applicationType = typeAccessor.getApplicationType(
                    RemotePluginContainerApplicationType.class);
            try
            {
                link = applicationLinkService.getApplicationLink(expectedApplicationId);

                if (link != null)
                {
                    if (getPluginKey().equals(link.getProperty(PLUGIN_KEY_PROPERTY)))
                    {
                        log.info("Application link for remote plugin container '{}' already exists", getPluginKey());
                    }
                    else
                    {
                        throw new PluginParseException("Application link already exists for id '" + expectedApplicationId + "' but it isn't the target " +
                                " plugin '" + getPluginKey() + "'");
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
            }
            catch (TypeNotInstalledException e)
            {
                throw new IllegalStateException("Missing type, should never happen", e);
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

    @Override
    public void disabled()
    {
        super.disabled();
        if (remoteMode)
        {
            for (ApplicationLink link : applicationLinkService.getApplicationLinks())
            {
                if (displayUrl.equals(link.getRpcUrl()))
                {
                    log.info("Removing application link for display url '{}'", displayUrl);
                    applicationLinkService.deleteApplicationLink(link);
                }
            }
            oAuthLinkManager.unassociateConsumer(
                    Consumer.
                                    key(getPluginKey()).
                            name("Doesn't Matter").
                            signatureMethod(Consumer.SignatureMethod.HMAC_SHA1).build());
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

    @Override
    public Void getModule()
    {
        return null;
    }
}
