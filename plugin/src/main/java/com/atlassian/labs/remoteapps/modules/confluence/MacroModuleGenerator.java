package com.atlassian.labs.remoteapps.modules.confluence;

import com.atlassian.labs.remoteapps.modules.external.DocumentBasedSchema;
import com.atlassian.labs.remoteapps.modules.external.Schema;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import org.dom4j.Element;

/**
 *
 */
public class MacroModuleGenerator extends AbstractMacroModuleGenerator
{
    private final Plugin plugin;
    public MacroModuleGenerator(PluginAccessor pluginAccessor, PluginRetrievalService pluginRetrievalService)
    {
        super(pluginAccessor);
        this.plugin = pluginRetrievalService.getPlugin();
    }

    @Override
    public String getType()
    {
        return "macro";
    }

    @Override
    public String getName()
    {
        return "Macro";
    }

    @Override
    public String getDescription()
    {
        return "A Confluence macro that returns XHTML in the Confluence storage format to be cached for at least one hour";
    }

    @Override
    public Schema getSchema()
    {
        return DocumentBasedSchema.builder("macro")
                .setPlugin(plugin)
                .setName(getName())
                .setDescription(getDescription())
                .build();
    }

    @Override
    public void generatePluginDescriptor(Element descriptorElement, Element pluginDescriptorRoot)
    {
        Element copy = descriptorElement.createCopy("remote-macro");
        pluginDescriptorRoot.add(copy);
    }

}
