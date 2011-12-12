package com.atlassian.labs.remoteapps.modules.oauth;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.labs.remoteapps.OAuthLinkManager;
import com.atlassian.labs.remoteapps.modules.external.RemoteAppCreationContext;
import com.atlassian.labs.remoteapps.modules.external.RemoteModule;
import com.atlassian.labs.remoteapps.modules.external.RemoteModuleGenerator;
import com.atlassian.labs.remoteapps.modules.external.StartableRemoteModule;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.ServiceProvider;
import com.atlassian.oauth.util.RSAKeys;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginInformation;
import com.atlassian.plugin.PluginParseException;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.Map;
import java.util.Set;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.*;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;

/**
 * Sets up a 2LO connection to allow incoming requests from the remote app
 */
public class OauthModuleGenerator implements RemoteModuleGenerator
{
    private final ApplicationLinkService applicationLinkService;

    private final OAuthLinkManager oAuthLinkManager;

    public OauthModuleGenerator(ApplicationLinkService applicationLinkService, OAuthLinkManager oAuthLinkManager)
    {
        this.applicationLinkService = applicationLinkService;
        this.oAuthLinkManager = oAuthLinkManager;
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
    public Map<String, String> getI18nMessages(String pluginKey, Element element)
    {
        return emptyMap();
    }

    @Override
    public RemoteModule generate(final RemoteAppCreationContext ctx, Element e)
    {
        final String key = getOptionalAttribute(e, "key", ctx.getApplicationType().getId().get());
        final PluginInformation pluginInfo = ctx.getPlugin().getPluginInformation();
        final String name = ctx.getApplicationType().getI18nKey();
        final String description = pluginInfo.getDescription();
        // todo: this is crap, get it from somewhere else
        String baseUrl = e.getParent().attributeValue("display-url");
        final URI callback = URI.create(baseUrl + getOptionalAttribute(e, "callback", "/callback"));
        final PublicKey publicKey = getPublicKey(getRequiredElementText(e, "public-key"));
        final URI requestTokenUrl = URI.create(baseUrl + getOptionalAttribute(e, "request-token-url", "/request-token"));
        final URI accessTokenUrl = URI.create(baseUrl + getOptionalAttribute(e, "access-token-url", "/access-token"));
        final URI authorizeUrl = URI.create(baseUrl + getOptionalAttribute(e, "authorize-url", "/authorize"));

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

                oAuthLinkManager.associateConsumerWithLink(link, consumer);

                oAuthLinkManager.associateProviderWithLink(link, consumer.getKey(), new ServiceProvider(requestTokenUrl, accessTokenUrl, authorizeUrl));

            }
        };
    }

    @Override
    public void validate(Element element) throws PluginParseException
    {
    }

    @Override
    public void convertDescriptor(Element descriptorElement, Element pluginDescriptorRoot)
    {
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
