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
public class VendorModuleGenerator implements RemoteModuleGenerator
{
    private final Plugin plugin;

    @Autowired
    public VendorModuleGenerator(PluginRetrievalService pluginRetrievalService)
    {
        this.plugin = pluginRetrievalService.getPlugin();
    }
      
    @Override
    public String getType()
    {
        return "vendor";
    }

    @Override
    public Schema getSchema()
    {
        return DocumentBasedSchema.builder("vendor")
                .setPlugin(plugin)
                .setName(getName())
                .setDescription(getDescription())
                .setMaxOccurs("1")
                .build();
    }

    @Override
    public String getName()
    {
        return "Vendor";
    }

    @Override
    public String getDescription()
    {
        return "Defines the remote app vendor information";
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
        pluginDescriptorRoot.element("plugin-info").add(descriptorElement.detach());
    }
}
