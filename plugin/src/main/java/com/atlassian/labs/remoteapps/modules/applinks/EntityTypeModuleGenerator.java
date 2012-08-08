package com.atlassian.labs.remoteapps.modules.applinks;

import com.atlassian.labs.remoteapps.modules.external.Schema;
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
 * Creates applink entity types
 */
@Component
public class EntityTypeModuleGenerator implements RemoteModuleGenerator
{
    private final Plugin plugin;

    @Autowired
    public EntityTypeModuleGenerator(PluginRetrievalService pluginRetrievalService)
    {
        this.plugin = pluginRetrievalService.getPlugin();
    }

    @Override
    public String getType()
    {
        return "entity-type";
    }

    @Override
    public String getName()
    {
        return "Entity Type";
    }

    @Override
    public String getDescription()
    {
        return "An application links entity type used for storing the relationship with a local " +
                "application entity like" +
                "            a JIRA project or Confluence space with a similar entity in the Remote App";
    }

    @Override
    public Schema getSchema()
    {
        return DocumentBasedSchema.builder("entity-type")
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
    public RemoteModule generate(RemoteAppCreationContext ctx, Element entity)
    {
        return RemoteModule.NO_OP;
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
