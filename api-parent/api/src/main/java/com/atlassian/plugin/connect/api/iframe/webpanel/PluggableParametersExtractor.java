package com.atlassian.plugin.connect.api.iframe.webpanel;

import com.atlassian.plugin.connect.api.iframe.context.ModuleContextParameters;

import java.util.Map;

/**
 * This component extracts context parameters based on local Connect project extractors as well as extractors loaded from plug-ins.
 */
public interface PluggableParametersExtractor
{
    ModuleContextParameters extractParameters(Map<String, Object> context);
}
