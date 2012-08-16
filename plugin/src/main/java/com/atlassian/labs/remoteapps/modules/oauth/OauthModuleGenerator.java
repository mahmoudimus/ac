package com.atlassian.labs.remoteapps.modules.oauth;

import com.atlassian.labs.remoteapps.modules.external.*;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Map;

import static java.util.Collections.emptyMap;

/**
 * Sets up a 2LO connection to allow incoming requests from the remote app
 *
 * This no longer does anything as it is handled by {@link com.atlassian.labs.remoteapps.modules.applinks.ApplicationTypeModuleGenerator}
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
        return DocumentBasedSchema.builder("oauth")
                .setPlugin(plugin)
                .setName(getName())
                .setDescription(getDescription())
                .setMaxOccurs("1")
                .build();
    }

    @Override
    public Map<String, String> getI18nMessages(String pluginKey, Element element)
    {
        return emptyMap();
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
