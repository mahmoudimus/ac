package com.atlassian.plugin.connect.plugin.capabilities.module;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;

/**
 *
 */
public interface MacroModuleContextExtractor
{
    ModuleContextParameters extractParameters(String storageFormatBody, ConversionContext conversionContext);
}
