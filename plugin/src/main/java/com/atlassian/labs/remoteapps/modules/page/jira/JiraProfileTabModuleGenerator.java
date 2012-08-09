package com.atlassian.labs.remoteapps.modules.page.jira;

import com.atlassian.labs.remoteapps.modules.external.*;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import org.dom4j.Element;

import java.net.URI;
import java.util.Map;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getRequiredUriAttribute;
import static java.util.Collections.emptyMap;

/**
 *
 */
public class JiraProfileTabModuleGenerator implements RemoteModuleGenerator
{
    private final Plugin plugin;

    public JiraProfileTabModuleGenerator(PluginRetrievalService pluginRetrievalService)
    {
        this.plugin = pluginRetrievalService.getPlugin();
    }

    @Override
    public String getType()
    {
        return "profile-page";
    }

    @Override
    public String getName()
    {
        return "User Profile Page";
    }

    @Override
    public String getDescription()
    {
        return "A user profile page decorated as a user profile tab";
    }

    @Override
    public Schema getSchema()
    {
        return DocumentBasedSchema.builder("page")
                .setPlugin(plugin)
                .setTitle(getName())
                .setDescription(getDescription())
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
        getRequiredUriAttribute(element, "url");
    }

    @Override
    public void generatePluginDescriptor(Element descriptorElement, Element pluginDescriptorRoot)
    {
        Element copy = descriptorElement.createCopy();
        pluginDescriptorRoot.add(copy);
    }
}
