package com.atlassian.plugin.connect.plugin.iframe.webpanel;

import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;

import java.util.Map;

/**
 *
 */
public interface WebFragmentModuleContextExtractor
{
    ModuleContextParameters extractParameters(Map<String, ? extends Object> webFragmentContext);
}
