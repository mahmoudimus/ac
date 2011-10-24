package com.atlassian.labs.remoteapps.modules.oauth;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.auth.types.OAuthAuthenticationProvider;
import com.atlassian.applinks.spi.auth.AuthenticationConfigurationManager;
import com.atlassian.labs.remoteapps.modules.RemoteAppCreationContext;
import com.atlassian.labs.remoteapps.modules.RemoteModule;
import com.atlassian.labs.remoteapps.modules.RemoteModuleGenerator;
import com.atlassian.labs.remoteapps.modules.StartableRemoteModule;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.oauth.serviceprovider.ServiceProviderConsumerStore;
import com.atlassian.oauth.util.RSAKeys;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginInformation;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.dom4j.Element;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;

/**
 *
 */
public class OauthModuleGenerator implements RemoteModuleGenerator
{
    private final ConsumerService consumerService;
    private final ServiceProviderConsumerStore serviceProviderConsumerStore;
    private final ApplicationLinkService applicationLinkService;
    private final AuthenticationConfigurationManager authenticationConfigurationManager;

    public OauthModuleGenerator(ConsumerService consumerService, ServiceProviderConsumerStore serviceProviderConsumerStore, ApplicationLinkService applicationLinkService, AuthenticationConfigurationManager authenticationConfigurationManager)
    {
        this.consumerService = consumerService;
        this.serviceProviderConsumerStore = serviceProviderConsumerStore;
        this.applicationLinkService = applicationLinkService;
        this.authenticationConfigurationManager = authenticationConfigurationManager;
    }

    @Override
    public String getType()
    {
        return "oauth";
    }

    @Override
    public Set<String> getDynamicModuleTypeDependencies()
    {
        return emptySet();
    }

    @Override
    public RemoteModule generate(final RemoteAppCreationContext ctx, Element e)
    {
        final String key = e.attributeValue("key") != null ? e.attributeValue("key") : ctx.getApplicationType().getId().get();
        final PluginInformation pluginInfo = ctx.getPlugin().getPluginInformation();
        final String name = ctx.getApplicationType().getI18nKey();
        final String description = pluginInfo.getDescription();
        final URI callback = URI.create(e.attributeValue("callback"));
        final PublicKey publicKey = getPublicKey(e.element("public-key").getTextTrim());
        final String requestTokenUrl = e.attributeValue("requestTokenUrl");
        final String accessTokenUrl = e.attributeValue("accessTokenUrl");
        final String authorizeUrl = e.attributeValue("authorizeUrl");

        return new StartableRemoteModule()
        {
            @Override
            public Set<ModuleDescriptor> getModuleDescriptors()
            {
                return emptySet();
            }

            @Override
            public void start()
            {
                ApplicationLink link = applicationLinkService.getPrimaryApplicationLink(ctx.getApplicationType().getClass());
                Consumer consumer = Consumer.key(key).name(name).publicKey(publicKey).description(description).callback(callback).build();

                if (serviceProviderConsumerStore.get(key) != null)
                {
                    serviceProviderConsumerStore.remove(key);
                }

                // fixme: this logic was copied from ual
                serviceProviderConsumerStore.put(consumer);
                link.putProperty("oauth.incoming.consumerkey", consumer.getKey());

                if (authenticationConfigurationManager.isConfigured(link.getId(), OAuthAuthenticationProvider.class))
                {
                    authenticationConfigurationManager.unregisterProvider(link.getId(), OAuthAuthenticationProvider.class);
                }
                authenticationConfigurationManager.registerProvider(
                    link.getId(),
                    OAuthAuthenticationProvider.class,
                    ImmutableMap.of(
                            "consumerKey.outbound", consumer.getKey(),
                            "serviceProvider.requestTokenUrl", requestTokenUrl,
                            "serviceProvider.accessTokenUrl", accessTokenUrl,
                            "serviceProvider.authorizeUrl", authorizeUrl
                            ));

            }
        };
    }

    protected final PublicKey getPublicKey(String publicKeyText)
    {
        PublicKey publicKey = null;
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
            throw new RuntimeException("Invalid public key", e);
        }
        return publicKey;
    }
}
