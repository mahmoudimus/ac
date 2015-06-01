package com.atlassian.plugin.connect.plugin.applinks;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.TypeNotInstalledException;
import com.atlassian.applinks.spi.application.ApplicationIdUtil;
import com.atlassian.applinks.spi.link.ApplicationLinkDetails;
import com.atlassian.applinks.spi.link.MutatingApplicationLinkService;
import com.atlassian.applinks.spi.util.TypeAccessor;
import com.atlassian.fugue.Option;
import com.atlassian.jwt.JwtConstants;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.ServiceProvider;
import com.atlassian.oauth.util.RSAKeys;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginInformation;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.OAuthLinkManager;
import com.atlassian.plugin.connect.spi.AuthenticationMethod;
import com.atlassian.plugin.connect.spi.applinks.MutatingApplicationLinkServiceProvider;
import com.atlassian.plugin.connect.spi.applinks.RemotePluginContainerApplicationType;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserKey;
import com.google.common.base.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.List;
import javax.annotation.Nullable;
import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkNotNull;

@ExportAsDevService
@Component
public class DefaultConnectApplinkManager implements ConnectApplinkManager
{
    public static final String PLUGIN_KEY_PROPERTY = JwtConstants.AppLinks.ADD_ON_ID_PROPERTY_NAME;
    private static final Logger log = LoggerFactory.getLogger(DefaultConnectApplinkManager.class);
    protected final MutatingApplicationLinkService applicationLinkService;
    private final TypeAccessor typeAccessor;
    private final PluginSettingsFactory pluginSettingsFactory;
    private final OAuthLinkManager oAuthLinkManager;
    protected final TransactionTemplate transactionTemplate;

    @Inject
    public DefaultConnectApplinkManager(MutatingApplicationLinkServiceProvider applicationLinkServiceProvider, TypeAccessor typeAccessor, PluginSettingsFactory pluginSettingsFactory, OAuthLinkManager oAuthLinkManager, TransactionTemplate transactionTemplate)
    {
        this.applicationLinkService = applicationLinkServiceProvider.getMutatingApplicationLinkService();
        this.typeAccessor = typeAccessor;
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.oAuthLinkManager = oAuthLinkManager;
        this.transactionTemplate = transactionTemplate;
    }

    /**
     * @deprecated use {@code createAppLink(final ConnectAddonBean addon, final String baseUrl, final AuthenticationType authType, final String publicKey, final UserKey addonUserKey)} instead
     */
    @Deprecated
    @Override
    public void createAppLink(final Plugin plugin, final String baseUrl, final AuthenticationType authType, final String publicKey, final String addonUserKey)
    {
        checkNotNull(addonUserKey);
        transactionTemplate.execute(new TransactionCallback<Void>()
        {
            @Override
            public Void doInTransaction()
            {
                String pluginKey = plugin.getKey();
                URI baseUri = URI.create(baseUrl);

                final ApplicationId expectedApplicationId = ApplicationIdUtil.generate(baseUri);

                final RemotePluginContainerApplicationType applicationType = typeAccessor.getApplicationType(RemotePluginContainerApplicationType.class);

                deleteOldAppLinks(pluginKey, expectedApplicationId);

                final ApplicationLinkDetails details = ApplicationLinkDetails.builder()
                        .displayUrl(baseUri)
                        .isPrimary(false)
                        .name(plugin.getName() != null ? plugin.getName() : plugin.getKey())
                        .rpcUrl(baseUri)
                        .build();

                log.info("Creating an application link for Connect add-on with key '{}'", pluginKey);

                ApplicationLink link = applicationLinkService.addApplicationLink(expectedApplicationId, applicationType, details);

                link.putProperty(PLUGIN_KEY_PROPERTY, pluginKey);
                link.putProperty(JwtConstants.AppLinks.ADD_ON_USER_KEY_PROPERTY_NAME, addonUserKey);
                link.putProperty("IS_ACTIVITY_ITEM_PROVIDER", Boolean.FALSE.toString());
                link.putProperty("system", Boolean.TRUE.toString());

                ServiceProvider serviceProvider = createServiceProvider();
                switch (authType)
                {
                    case JWT:
                        link.putProperty(AuthenticationMethod.PROPERTY_NAME, AuthenticationMethod.JWT.toString());
                        link.putProperty(JwtConstants.AppLinks.SHARED_SECRET_PROPERTY_NAME, publicKey);
                        break;
                    case OAUTH:
                        oAuthLinkManager.associateProviderWithLink(link, applicationType.getId().get(), serviceProvider);
                        registerOAuth(link, plugin, publicKey);
                        break;
                    case NONE:
                        link.putProperty(AuthenticationMethod.PROPERTY_NAME, AuthenticationMethod.NONE.toString());
                        break;
                    default:
                        log.warn("Unknown authType encountered: " + authType.name());
                }
                return null;
            }
        });
    }

    @Override
    public void createAppLink(final ConnectAddonBean addon, final String baseUrl,
                              final AuthenticationType authType, final String publicKey,
                              final UserKey addonUserKey)
    {
        transactionTemplate.execute(new TransactionCallback<Void>()
        {
            @Override
            public Void doInTransaction()
            {
                String pluginKey = addon.getKey();
                URI baseUri = URI.create(baseUrl);

                final ApplicationId expectedApplicationId = ApplicationIdUtil.generate(baseUri);

                final RemotePluginContainerApplicationType applicationType = typeAccessor.getApplicationType(RemotePluginContainerApplicationType.class);

                deleteOldAppLinks(pluginKey, expectedApplicationId);

                final ApplicationLinkDetails details = ApplicationLinkDetails.builder()
                        .displayUrl(baseUri)
                        .isPrimary(false)
                        .name(addon.getName() != null ? addon.getName() : addon.getKey())
                        .rpcUrl(baseUri)
                        .build();

                log.info("Creating an application link for Connect add-on with key '{}'", pluginKey);

                ApplicationLink link = applicationLinkService.addApplicationLink(expectedApplicationId, applicationType, details);

                link.putProperty(PLUGIN_KEY_PROPERTY, pluginKey);
                link.putProperty(JwtConstants.AppLinks.ADD_ON_USER_KEY_PROPERTY_NAME, addonUserKey.getStringValue());
                link.putProperty("IS_ACTIVITY_ITEM_PROVIDER", Boolean.FALSE.toString());
                link.putProperty("system", Boolean.TRUE.toString());

                ServiceProvider serviceProvider = createServiceProvider();
                switch (authType)
                {
                    case JWT:
                        link.putProperty(AuthenticationMethod.PROPERTY_NAME, AuthenticationMethod.JWT.toString());
                        link.putProperty(JwtConstants.AppLinks.SHARED_SECRET_PROPERTY_NAME, publicKey);
                        break;
                    case OAUTH:
                        oAuthLinkManager.associateProviderWithLink(link, applicationType.getId().get(), serviceProvider);
                        registerOAuth(link, addon, publicKey);
                        break;
                    case NONE:
                        link.putProperty(AuthenticationMethod.PROPERTY_NAME, AuthenticationMethod.NONE.toString());
                        break;
                    default:
                        log.warn("Unknown authType encountered: " + authType.name());
                }

                return null;
            }
        });
    }

    @Override
    public void deleteAppLink(final ConnectAddonBean addon) throws NotConnectAddonException
    {
        final String key = addon.getKey();
        deleteAppLink(key);
    }

    @Override
    public void deleteAppLink(final String key) throws NotConnectAddonException
    {
        final ApplicationLink link = getAppLink(key);

        if (link != null)
        {
            transactionTemplate.execute(new TransactionCallback<Void>()
            {
                @Override
                public Void doInTransaction()
                {
                    log.info("Removing application link for {}", key);
                    applicationLinkService.deleteApplicationLink(link);
                    return null;
                }
            });
        }
        else
        {
            log.debug("Could not remove application link for {}", key);
        }
    }

    @Override
    public ApplicationLink getAppLink(String key) throws NotConnectAddonException
    {
        for (ApplicationLink link : applicationLinkService.getApplicationLinks())
        {
            if (key.equals(link.getProperty(PLUGIN_KEY_PROPERTY)))
            {
                if (link.getType() instanceof RemotePluginContainerApplicationType)
                {
                    return link;
                }
                else
                {
                    throw new NotConnectAddonException("Plugin " + key + " does not seem to be a Connect add-on. It's type is: " + link.getType().getClass().getSimpleName());
                }
            }
        }
        return null;
    }

    @Override
    public URI getApplinkLinkSelfLink(final ApplicationLink applink)
    {
        return applicationLinkService.createSelfLinkFor(applink.getId());
    }

    @Override
    public Option<String> getSharedSecretOrPublicKey(ApplicationLink applink)
    {
        Option<AuthenticationType> maybeAuthType = ConnectApplinkUtil.getAuthenticationType(applink);
        if (maybeAuthType.isDefined())
        {
            if (maybeAuthType.get().equals(AuthenticationType.JWT))
            {
                Object prop = applink.getProperty(JwtConstants.AppLinks.SHARED_SECRET_PROPERTY_NAME);
                if (prop instanceof String)
                {
                    return Option.some((String) prop);
                }
            }
            else if (maybeAuthType.get().equals(AuthenticationType.OAUTH))
            {
                Option<PublicKey> maybePublicKey = this.oAuthLinkManager.getPublicKeyFromLink(applink);
                return maybePublicKey.flatMap(new Function<PublicKey, Option<String>>()
                {
                    @Override
                    public Option<String> apply(@Nullable PublicKey input)
                    {
                        if (input == null)
                        {
                            Option.none();
                        }
                        return Option.option(RSAKeys.toPemEncoding(input));
                    }
                });
            }
        }
        return Option.none();
    }

    private void deleteOldAppLinks(String pluginKey, ApplicationId appId)
    {
        ApplicationLink link;

        try
        {
            link = applicationLinkService.getApplicationLink(appId);
        }
        catch (TypeNotInstalledException ex)
        {
            log.warn("Link found for '{}' but the type cannot be found, deleting...", pluginKey);
            manuallyDeleteApplicationId(appId);

            return;
        }

        if (null != link)
        {
            if (pluginKey.equals(link.getProperty(PLUGIN_KEY_PROPERTY)))
            {
                // This shouldn't happen in normal operation as we delete the applink when we uninstall an addon
                // and we uninstall the old addon when we install a new version
                log.warn("Application link for remote plugin container '{}' already exists. Deleting", pluginKey);

                applicationLinkService.deleteApplicationLink(link);

            }
            else
            {
                throw new IllegalStateException("Application link already exists for id '" + appId + "' but it isn't the target " +
                        " plugin '" + pluginKey + "': unexpected plugin key is: " + link.getProperty(PLUGIN_KEY_PROPERTY));
            }
        }
        else
        {
            // try to find link with old display url
            for (final ApplicationLink otherLink : applicationLinkService.getApplicationLinks(RemotePluginContainerApplicationType.class))
            {
                if (pluginKey.equals(otherLink.getProperty(PLUGIN_KEY_PROPERTY)))
                {
                    log.debug("Old application link for this plugin '{}' found with different display url '{}', removing", pluginKey, otherLink.getDisplayUrl());

                    applicationLinkService.deleteApplicationLink(otherLink);
                }
            }

        }

    }

    @SuppressWarnings ("unchecked")
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

    private ServiceProvider createServiceProvider()
    {
        URI dummyUri = URI.create("http://localhost");
        return new ServiceProvider(dummyUri, dummyUri, dummyUri);
    }

    /**
     * @deprecated use {@code registerOAuth(ApplicationLink link, ConnectAddonBean addon, String publicKey)} instead
     */
    @Deprecated
    private void registerOAuth(ApplicationLink link, Plugin plugin, String publicKey)
    {
        String pluginKey = plugin.getKey();

        final PluginInformation pluginInfo = plugin.getPluginInformation();
        final String name = plugin.getName();
        final String description = pluginInfo.getDescription();

        final PublicKey publicKeyObj = getPublicKey(publicKey);

        Consumer consumer = Consumer.key(pluginKey).name(name != null ? name : pluginKey).publicKey(publicKeyObj).description(description).build();

        oAuthLinkManager.associateConsumerWithLink(link, consumer);
    }

    private void registerOAuth(ApplicationLink link, ConnectAddonBean addon, String publicKey)
    {
        String pluginKey = addon.getKey();

        final String name = addon.getName();
        final String description = addon.getDescription();

        final PublicKey publicKeyObj = getPublicKey(publicKey);

        Consumer consumer = Consumer.key(pluginKey).name(name != null ? name : pluginKey).publicKey(publicKeyObj).description(description).build();

        oAuthLinkManager.associateConsumerWithLink(link, consumer);
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
}
