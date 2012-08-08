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
public class PageMacroModuleGenerator extends AbstractMacroModuleGenerator
{
    private final Plugin plugin;

    public PageMacroModuleGenerator(PluginAccessor pluginAccessor,
            PluginRetrievalService pluginRetrievalService)
    {
        super(pluginAccessor);
        this.plugin = pluginRetrievalService.getPlugin();
    }

    @Override
    public String getType()
    {
        return "page-macro";
    }

    @Override
    public String getName()
    {
        return "Page Macro";
    }

    @Override
    public String getDescription()
    {
        return "A Confluence macro that loads the remote page as an IFrame";
    }

    @Override
    public Schema getSchema()
    {
        return DocumentBasedSchema.builder("page-macro")
                .setPlugin(plugin)
                .setTitle(getName())
                .setDescription(getDescription())
                .build();
    }

    @Override
    public void generatePluginDescriptor(Element descriptorElement, Element pluginDescriptorRoot)
    {
        Element copy = descriptorElement.createCopy("macro-page");
        pluginDescriptorRoot.add(copy);
    }
}
