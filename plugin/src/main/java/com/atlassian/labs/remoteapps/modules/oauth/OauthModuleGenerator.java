package com.atlassian.labs.remoteapps.modules.oauth;

import com.atlassian.labs.remoteapps.ApplicationLinkAccessor;
import com.atlassian.labs.remoteapps.OAuthLinkManager;
import com.atlassian.labs.remoteapps.modules.external.*;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.ServiceProvider;
import com.atlassian.oauth.util.RSAKeys;
import com.atlassian.plugin.ModuleDescriptor;
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
import java.util.Set;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getOptionalAttribute;
import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getRequiredElementText;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;

/**
 * Sets up a 2LO connection to allow incoming requests from the remote app
 */
@Component
public class OauthModuleGenerator implements RemoteModuleGenerator
{
    private final Plugin plugin;

    @Autowired
    public OauthModuleGenerator(PluginRetrievalService pluginRetrievalService)
    {
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
        return new RemoteModule()
        {
            @Override
            public Set<ModuleDescriptor> getModuleDescriptors()
            {
                return emptySet();
            }
        };
    }

    @Override
    public void validate(Element element, URI registrationUrl, String username) throws PluginParseException
    {
    }

    @Override
    public void generatePluginDescriptor(Element descriptorElement, Element pluginDescriptorRoot)
    {
    }

}
