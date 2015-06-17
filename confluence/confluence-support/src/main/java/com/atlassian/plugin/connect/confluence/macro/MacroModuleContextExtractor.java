package com.atlassian.plugin.connect.confluence.macro;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.plugin.connect.api.iframe.context.ModuleContextParameters;

import java.util.Map;

/**
 *
 */
public interface MacroModuleContextExtractor
{
    ModuleContextParameters extractParameters(String storageFormatBody, ConversionContext conversionContext,
            Map<String, String> parameters);
}
