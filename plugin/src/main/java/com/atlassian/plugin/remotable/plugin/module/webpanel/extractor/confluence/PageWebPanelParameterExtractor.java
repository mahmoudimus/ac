package com.atlassian.plugin.remotable.plugin.module.webpanel.extractor.confluence;

import com.atlassian.confluence.plugin.descriptor.web.WebInterfaceContext;
import com.atlassian.plugin.remotable.plugin.module.webpanel.extractor.WebPanelParameterExtractor;
import com.google.common.collect.ImmutableMap;

import java.util.Collections;
import java.util.Map;

/**
 * Extracts page parameters that can be included in webpanel's iframe url.
 */
public class PageWebPanelParameterExtractor implements WebPanelParameterExtractor
{
    @Override
    public Map<String, Object> extract(final Map<String, Object> context)
    {
        if (context.containsKey("webInterfaceContext"))
        {
            WebInterfaceContext webInterfaceContext = (WebInterfaceContext) context.get("webInterfaceContext");
            if (null != webInterfaceContext && null != webInterfaceContext.getPage())
            {
                return ImmutableMap.<String, Object>of("page",
                        ImmutableMap.of("id", webInterfaceContext.getPage().getId())
                );
            }
        }
        return Collections.emptyMap();
    }
}
