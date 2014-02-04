package com.atlassian.plugin.connect.plugin.iframe.webpanel;

import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;

import java.util.Map;

/**
 *
 */
public interface WebPanelModuleContextExtractor
{
    ModuleContextParameters extractParameters(Map<String, Object> webPanelContext);
}
