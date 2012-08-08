package com.atlassian.labs.remoteapps.modules.jira.issuepanel;

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
public class ViewIssuePanelModuleGenerator implements RemoteModuleGenerator
{
    private final Plugin plugin;

    public ViewIssuePanelModuleGenerator(PluginRetrievalService pluginRetrievalService)
    {
        this.plugin = pluginRetrievalService.getPlugin();
    }

    @Override
    public String getType()
    {
        return "issue-panel-page";
    }

    @Override
    public Schema getSchema()
    {
        return DocumentBasedSchema.builder("panel")
                .setPlugin(plugin)
                .setTitle(getName())
                .setDescription(getDescription())
                .build();
    }

    @Override
    public RemoteModule generate(final RemoteAppCreationContext ctx, final Element element)
    {
        return RemoteModule.NO_OP;
    }

    @Override
    public void validate(Element element, URI registrationUrl, String username) throws PluginParseException
    {
        getRequiredUriAttribute(element, "url");
    }

    @Override
    public void generatePluginDescriptor(Element descriptorElement, Element pluginDescriptorRoot)
    {
        Element copy = descriptorElement.createCopy("issue-panel-page");
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
        return "Issue Tab Page";
    }

    @Override
    public String getDescription()
    {
        return "A remote page decorated as a web panel on the view issue page";
    }
}
