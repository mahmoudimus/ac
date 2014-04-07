package com.atlassian.plugin.connect.plugin.module.confluence;

import com.atlassian.confluence.macro.browser.beans.MacroMetadata;
import com.atlassian.confluence.plugin.descriptor.MacroMetadataParser;
import com.atlassian.confluence.plugin.descriptor.XhtmlMacroModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.util.concurrent.NotNull;

import org.dom4j.Element;

/**
 * Fixes descriptors that treat absolute icon urls as relative
 *
 * See https://jira.atlassian.com/browse/CONF-25394
 */
public class FixedXhtmlMacroModuleDescriptor extends XhtmlMacroModuleDescriptor
{
    private final String completeKey;

    public FixedXhtmlMacroModuleDescriptor(ModuleFactory moduleFactory, MacroMetadataParser metadataParser)
    {
        super(moduleFactory, metadataParser);
        this.completeKey = null;
    }
    
    public FixedXhtmlMacroModuleDescriptor(ModuleFactory moduleFactory, MacroMetadataParser metadataParser, String completeKey)
    {
        super(moduleFactory, metadataParser);
        this.completeKey = completeKey;
    }

    @Override
    public void init(@NotNull Plugin plugin, @NotNull Element element) throws PluginParseException
    {
        super.init(plugin, element);

    }

    @Override
    public MacroMetadata getMacroMetadata()
    {
        return new FixedMacroMetadata(super.getMacroMetadata());
    }

    @Override
    public String getCompleteKey()
    {
        return (null != completeKey) ? completeKey : super.getCompleteKey();
    }
}
