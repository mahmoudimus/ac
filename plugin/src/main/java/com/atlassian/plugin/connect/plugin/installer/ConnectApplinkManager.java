package com.atlassian.plugin.connect.plugin.installer;

import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

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
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named
public class ConnectApplinkManager
{
    public static final String PLUGIN_KEY_PROPERTY = "plugin-key";
    private static final Logger log = LoggerFactory.getLogger(ConnectApplinkManager.class);
    private final MutatingApplicationLinkService applicationLinkService;
    private final TypeAccessor typeAccessor;
    private final PluginSettingsFactory pluginSettingsFactory;
    private final OAuthLinkManager oAuthLinkManager;
    private final PermissionManager permissionManager;
    private final TransactionTemplate transactionTemplate;

    @Inject
    public ConnectApplinkManager(MutatingApplicationLinkService applicationLinkService, TypeAccessor typeAccessor, PluginSettingsFactory pluginSettingsFactory, OAuthLinkManager oAuthLinkManager, PermissionManager permissionManager, TransactionTemplate transactionTemplate)
    {
        this.applicationLinkService = applicationLinkService;
        this.typeAccessor = typeAccessor;
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.oAuthLinkManager = oAuthLinkManager;
        this.permissionManager = permissionManager;
        this.transactionTemplate = transactionTemplate;
    }

    public void createAppLink(final Plugin plugin, final String baseUrl, final AuthenticationType authType, final String sharedKey)
    {
        transactionTemplate.execute(new TransactionCallback<Void>() {
            @Override
            public Void doInTransaction()
            {
                String pluginKey = plugin.getKey();
                URI baseUri = URI.create(baseUrl);

                final ApplicationId expectedApplicationId = ApplicationIdUtil.generate(baseUri);

                ApplicationLink link = null;
                final RemotePluginContainerApplicationType applicationType = typeAccessor.getApplicationType(RemotePluginContainerApplicationType.class);

                if (!compatibleApplinkExists(pluginKey, expectedApplicationId))
                {
                    final ApplicationLinkDetails details = ApplicationLinkDetails.builder()
                                                                                 .displayUrl(baseUri)
                            .isPrimary(false)
                                    // todo: support i18n names
                            .name(plugin.getName() != null ? plugin.getName() : plugin.getKey())
                            .rpcUrl(baseUri)
                            .build();

                    log.info("Creating an application link for the remote plugin container of key '{}'", pluginKey);

                    link = applicationLinkService.addApplicationLink(expectedApplicationId, applicationType, details);

                    link.putProperty(PLUGIN_KEY_PROPERTY, pluginKey);


                    link.putProperty("IS_ACTIVITY_ITEM_PROVIDER", Boolean.FALSE.toString());
                    link.putProperty("system", Boolean.TRUE.toString());

                    ServiceProvider serviceProvider = createServiceProvider();
                    switch (authType)
                    {
                        case JWT :
                            //TODO: npt sure what to do here.
                            break;
                        case OAUTH:
                            oAuthLinkManager.associateProviderWithLink(link, applicationType.getId().get(), serviceProvider);
                            registerOAuth(link, plugin, sharedKey,baseUri);
                            break;
                    }

                }
                return null;
            }
        });
        

    }

    private boolean compatibleApplinkExists(String pluginKey, ApplicationId appId)
    {
        ApplicationLink link = null;

        try
        {
            link = applicationLinkService.getApplicationLink(appId);
        }
        catch (TypeNotInstalledException ex)
        {
            log.info("Link found for '{}' but the type cannot be found, treating as not found", pluginKey);
            manuallyDeleteApplicationId(appId);

            return false;
        }

        if (null != link)
        {
            if (pluginKey.equals(link.getProperty(PLUGIN_KEY_PROPERTY)))
            {
                log.info("Application link for remote plugin container '{}' already exists", pluginKey);

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
                    log.debug("Old application link for this plugin '{}' found with different display url '{}', removing",
                            pluginKey, otherLink.getDisplayUrl());

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
