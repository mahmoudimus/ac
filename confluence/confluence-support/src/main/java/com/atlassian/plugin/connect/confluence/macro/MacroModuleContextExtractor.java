package com.atlassian.plugin.connect.confluence.macro;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;

import java.util.Map;

/**
 *
 */
public interface MacroModuleContextExtractor
{
    Map<String, String> extractParameters(String storageFormatBody, ConversionContext conversionContext,
            Map<String, String> parameters);
}
