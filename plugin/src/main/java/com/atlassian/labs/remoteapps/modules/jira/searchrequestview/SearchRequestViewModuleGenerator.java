package com.atlassian.labs.remoteapps.modules.jira.searchrequestview;

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
 * A module that maps the search-request-view plugin module to remote apps
 */
public class SearchRequestViewModuleGenerator implements RemoteModuleGenerator
{
    private final Plugin plugin;

    public SearchRequestViewModuleGenerator(PluginRetrievalService pluginRetrievalService)
    {
        this.plugin = pluginRetrievalService.getPlugin();
    }

    @Override
    public String getType()
    {
        return "search-request-view";
    }

    @Override
    public Schema getSchema()
    {
        return DocumentBasedSchema.builder("remote-search-request-view")
                .setPlugin(plugin)
                .setTitle(getName())
                .setDescription(getDescription())
                .build();
    }

    @Override
    public void validate(Element element, URI registrationUrl, String username) throws
            PluginParseException
    {
        getRequiredUriAttribute(element, "url");
    }

    @Override
    public void generatePluginDescriptor(Element descriptorElement, Element pluginDescriptorRoot)
    {
        Element copy = descriptorElement.createCopy("remote-search-request-view");
        pluginDescriptorRoot.add(copy);
    }

    @Override
    public Map<String, String> getI18nMessages(String pluginKey, Element element)
    {
        return emptyMap();
    }

    @Override
    public String getName()
    {
        return "Search Request View";
    }

    @Override
    public String getDescription()
    {
        return "A search request view that redirects to the Remote App's url with found issue" +
                " keys as the 'issues' query parameter";
    }

}
