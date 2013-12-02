package com.atlassian.plugin.connect.plugin.applinks;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.TypeNotInstalledException;
import com.atlassian.applinks.spi.application.ApplicationIdUtil;
import com.atlassian.applinks.spi.link.ApplicationLinkDetails;
import com.atlassian.applinks.spi.link.MutatingApplicationLinkService;
import com.atlassian.applinks.spi.util.TypeAccessor;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.ServiceProvider;
import com.atlassian.oauth.util.RSAKeys;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginInformation;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.plugin.OAuthLinkManager;
import com.atlassian.plugin.connect.plugin.PermissionManager;
import com.atlassian.plugin.connect.plugin.capabilities.beans.AuthenticationType;
import com.atlassian.plugin.connect.spi.Permissions;
import com.atlassian.plugin.connect.spi.applinks.RemotePluginContainerApplicationType;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.List;

@ExportAsDevService
@Named
public class DefaultConnectApplinkManager implements ConnectApplinkManager
{
    public static final String PLUGIN_KEY_PROPERTY = "plugin-key";
    private static final Logger log = LoggerFactory.getLogger(DefaultConnectApplinkManager.class);
    private final MutatingApplicationLinkService applicationLinkService;
    private final TypeAccessor typeAccessor;
    private final PluginSettingsFactory pluginSettingsFactory;
    private final OAuthLinkManager oAuthLinkManager;
    private final PermissionManager permissionManager;
    private final TransactionTemplate transactionTemplate;

    @Inject
    public DefaultConnectApplinkManager(MutatingApplicationLinkService applicationLinkService, TypeAccessor typeAccessor, PluginSettingsFactory pluginSettingsFactory, OAuthLinkManager oAuthLinkManager, PermissionManager permissionManager, TransactionTemplate transactionTemplate)
    {
        this.applicationLinkService = applicationLinkService;
        this.typeAccessor = typeAccessor;
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.oAuthLinkManager = oAuthLinkManager;
        this.permissionManager = permissionManager;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public void createAppLink(final Plugin plugin, final String baseUrl, final AuthenticationType authType, final String sharedKey)
    {
        transactionTemplate.execute(new TransactionCallback<Void>()
        {
            @Override
            public Void doInTransaction()
            {
                String pluginKey = plugin.getKey();
                URI baseUri = URI.create(baseUrl);

                final ApplicationId expectedApplicationId = ApplicationIdUtil.generate(baseUri);

                ApplicationLink link;
                final RemotePluginContainerApplicationType applicationType = typeAccessor.getApplicationType(RemotePluginContainerApplicationType.class);

                if (!compatibleAppLinkExists(pluginKey, expectedApplicationId))
                {
                    final ApplicationLinkDetails details = ApplicationLinkDetails.builder()
                            .displayUrl(baseUri)
                            .isPrimary(false)
                            .name(plugin.getName() != null ? plugin.getName() : plugin.getKey())
                            .rpcUrl(baseUri)
                            .build();

                    log.info("Creating an application link for Connect add-on with key '{}'", pluginKey);

                    link = applicationLinkService.addApplicationLink(expectedApplicationId, applicationType, details);

                    link.putProperty(PLUGIN_KEY_PROPERTY, pluginKey);


                    link.putProperty("IS_ACTIVITY_ITEM_PROVIDER", Boolean.FALSE.toString());
                    link.putProperty("system", Boolean.TRUE.toString());

                    ServiceProvider serviceProvider = createServiceProvider();
                    switch (authType)
                    {
                        case JWT:
                            //TODO: not sure what to do here.
                            break;
                        case OAUTH:
                            oAuthLinkManager.associateProviderWithLink(link, applicationType.getId().get(), serviceProvider);
                            registerOAuth(link, plugin, sharedKey, baseUri);
                            break;
                        default:
                            log.warn("Unknown authType encountered: " + authType.name());
                    }

                }
                return null;
            }
        });
    }

    @Override
    public void deleteAppLink(final Plugin plugin) throws NotConnectAddonException
    {
        final String key = plugin.getKey();
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

    private boolean compatibleAppLinkExists(String pluginKey, ApplicationId appId)
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

            return false;
        }

        if (null != link)
        {
            if (pluginKey.equals(link.getProperty(PLUGIN_KEY_PROPERTY)))
            {
                log.debug("Application link for remote plugin container '{}' already exists", pluginKey);

                return true;
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

            return false;
        }

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

    private ServiceProvider createServiceProvider()
    {
        URI dummyUri = URI.create("http://localhost");
        return new ServiceProvider(dummyUri, dummyUri, dummyUri);
    }

    private void registerOAuth(ApplicationLink link, Plugin plugin, String oauthKey, URI baseUri)
    {
        String pluginKey = plugin.getKey();
        permissionManager.requirePermission(pluginKey, Permissions.CREATE_OAUTH_LINK);

        final PluginInformation pluginInfo = plugin.getPluginInformation();
        final String name = plugin.getName();
        final String description = pluginInfo.getDescription();

        final PublicKey publicKey = getPublicKey(oauthKey);

        Consumer consumer = Consumer.key(pluginKey).name(name != null ? name : pluginKey).publicKey(publicKey).description(description).build();

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
