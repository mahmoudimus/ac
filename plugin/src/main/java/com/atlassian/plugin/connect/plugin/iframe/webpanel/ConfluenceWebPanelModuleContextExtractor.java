package com.atlassian.plugin.connect.plugin.iframe.webpanel;

import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;

import java.util.Map;

/**
 *
 */
@ConfluenceComponent
public class ConfluenceWebPanelModuleContextExtractor implements WebPanelModuleContextExtractor
{
    @Override
    public ModuleContextParameters extractParameters(final Map<String, Object> webPanelContext)
    {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
