package com.atlassian.labs.remoteapps.plugin.module.plugin;

import com.atlassian.labs.remoteapps.plugin.module.RemoteModuleGenerator;
import com.atlassian.labs.remoteapps.spi.schema.DocumentBasedSchema;
import com.atlassian.labs.remoteapps.spi.schema.Schema;
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
 *
 */
@Component
public class DescriptionModuleGenerator implements RemoteModuleGenerator
{
    private final Plugin plugin;

    @Autowired
    public DescriptionModuleGenerator(PluginRetrievalService pluginRetrievalService)
    {
        this.plugin = pluginRetrievalService.getPlugin();
    }
    @Override
    public String getType()
    {
        return "description";
    }

    @Override
    public Schema getSchema()
    {
        return DocumentBasedSchema.builder("description")
                .setPlugin(plugin)
                .setName(getName())
                .setDescription(getDescription())
                .setMaxOccurs("1")
                .build();
    }

    @Override
    public String getName()
    {
        return "DescriptionType";
    }

    @Override
    public String getDescription()
    {
        return "Defines the Remote App description";
    }

    @Override
    public void validate(Element element, URI registrationUrl, String username) throws PluginParseException
    {
    }

    @Override
    public void generatePluginDescriptor(Element descriptorElement, Element pluginDescriptorRoot)
    {
        pluginDescriptorRoot.element("plugin-info").addElement("description").setText(descriptorElement.getTextTrim());
    }
}
