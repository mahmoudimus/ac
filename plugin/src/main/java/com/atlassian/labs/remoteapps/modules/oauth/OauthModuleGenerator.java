package com.atlassian.labs.remoteapps.modules.oauth;

import com.atlassian.labs.remoteapps.ApplicationLinkAccessor;
import com.atlassian.labs.remoteapps.OAuthLinkManager;
import com.atlassian.labs.remoteapps.modules.external.*;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.ServiceProvider;
import com.atlassian.oauth.util.RSAKeys;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginInformation;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.Map;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getOptionalAttribute;
import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getRequiredElementText;
import static java.util.Collections.emptyMap;

/**
 * Sets up a 2LO connection to allow incoming requests from the remote app
 */
@Component
public class OauthModuleGenerator implements UninstallableRemoteModuleGenerator
{
    private final OAuthLinkManager oAuthLinkManager;
    private final ApplicationLinkAccessor applicationLinkAccessor;
    private final Plugin plugin;

    @Autowired
    public OauthModuleGenerator(ApplicationLinkAccessor applicationLinkAccessor,
                                OAuthLinkManager oAuthLinkManager, PluginRetrievalService pluginRetrievalService)
    {
        this.applicationLinkAccessor = applicationLinkAccessor;
        this.oAuthLinkManager = oAuthLinkManager;
        this.plugin = pluginRetrievalService.getPlugin();
    }

    @Override
    public String getType()
    {
        return "oauth";
    }

    @Override
    public String getName()
    {
        return "OAuth";
    }

    @Override
    public String getDescription()
    {
        return "Creates an outgoing oauth link to allow the host application to call the Remote App in an authenticated manner";
    }

    @Override
    public Schema getSchema()
    {
        return new StaticSchema(plugin,
                "oauth.xsd",
                "/xsd/oauth.xsd",
                "OauthType",
                "1");
    }

    @Override
    public Map<String, String> getI18nMessages(String pluginKey, Element element)
    {
        return emptyMap();
    }

    @Override
    public RemoteModule generate(final RemoteAppCreationContext ctx, Element e)
    {
        final String key = getOptionalAttribute(e, "key", ctx.getPlugin().getKey());
        final PluginInformation pluginInfo = ctx.getPlugin().getPluginInformation();
        final String name = ctx.getPlugin().getName();
        final String description = pluginInfo.getDescription();
        URI baseUrl = ctx.getRemoteAppAccessor().getDisplayUrl();
        final URI callback = URI.create(baseUrl + getOptionalAttribute(e, "callback", "/callback"));
        final PublicKey publicKey = getPublicKey(getRequiredElementText(e, "public-key"));
        final URI requestTokenUrl = URI.create(baseUrl + getOptionalAttribute(e, "request-token-url", "/request-token"));
        final URI accessTokenUrl = URI.create(baseUrl + getOptionalAttribute(e, "access-token-url", "/access-token"));
        final URI authorizeUrl = URI.create(baseUrl + getOptionalAttribute(e, "authorize-url", "/authorize"));

        return new OAuthModule(oAuthLinkManager, applicationLinkAccessor,
                               Consumer.key(key).name(name != null ? name : key).publicKey(publicKey).description(description).callback(
                                       callback).build(), new ServiceProvider(requestTokenUrl, accessTokenUrl, authorizeUrl),
                key);
    }

    @Override
    public void validate(Element element, URI registrationUrl, String username) throws PluginParseException
    {
    }

    @Override
    public void generatePluginDescriptor(Element descriptorElement, Element pluginDescriptorRoot)
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

    @Override
    public void uninstall(String pluginKey)
    {
        oAuthLinkManager.unassociateConsumer(
                Consumer.
                        key(pluginKey).
                        name("Doesn't Matter").
                        signatureMethod(Consumer.SignatureMethod.HMAC_SHA1).build());
    }
}
