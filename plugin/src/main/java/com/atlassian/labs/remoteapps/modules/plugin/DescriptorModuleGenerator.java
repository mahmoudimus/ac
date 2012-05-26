package com.atlassian.labs.remoteapps.modules.plugin;

import com.atlassian.labs.remoteapps.modules.external.*;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;

/**
 *
 */
@Component
public class DescriptorModuleGenerator implements RemoteModuleGenerator
{
    private final Plugin plugin;

    @Autowired
    public DescriptorModuleGenerator(PluginRetrievalService pluginRetrievalService)
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
        return new StaticSchema(plugin,
                "description.xsd",
                "/xsd/description.xsd",
                "DescriptionType",
                "1");
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
    public Map<String, String> getI18nMessages(String pluginKey, Element element)
    {
        return emptyMap();
    }

    @Override
    public RemoteModule generate(RemoteAppCreationContext ctx, Element element)
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
        pluginDescriptorRoot.element("plugin-info").addElement("description").setText(descriptorElement.getTextTrim());
    }
}
