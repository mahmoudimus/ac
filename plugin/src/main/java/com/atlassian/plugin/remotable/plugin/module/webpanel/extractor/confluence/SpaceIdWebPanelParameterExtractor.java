package com.atlassian.plugin.remotable.plugin.module.webpanel.extractor.confluence;

import com.atlassian.confluence.plugin.descriptor.web.WebInterfaceContext;
import com.atlassian.plugin.remotable.plugin.module.webpanel.extractor.WebPanelParameterExtractor;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Extracts space id that will be included in webpanel's iframe url.
 */
public class SpaceIdWebPanelParameterExtractor implements WebPanelParameterExtractor
{
    @Override
    public void extract(final Map<String, Object> context, final Map<String, Object> whiteListedContext)
    {
        if (context.containsKey("webInterfaceContext"))
        {
            WebInterfaceContext webInterfaceContext = (WebInterfaceContext) context.get("webInterfaceContext");
            if (null != webInterfaceContext && null != webInterfaceContext.getSpace())
            {
                whiteListedContext.put("space", ImmutableMap.of(
                        "id", webInterfaceContext.getSpace().getId(),
                        "key", webInterfaceContext.getSpace().getKey()
                ));
            }
        }
    }
}
