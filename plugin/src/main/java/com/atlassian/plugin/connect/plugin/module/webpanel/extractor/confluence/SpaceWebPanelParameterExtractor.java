package com.atlassian.plugin.connect.plugin.module.webpanel.extractor.confluence;

import java.util.Collections;
import java.util.Map;

import com.atlassian.confluence.plugin.descriptor.web.WebInterfaceContext;
import com.atlassian.plugin.connect.plugin.module.webpanel.extractor.WebPanelParameterExtractor;

import com.google.common.collect.ImmutableMap;

/**
 * Extracts space parameters that can be included in webpanel's iframe url.
 */
public class SpaceWebPanelParameterExtractor implements WebPanelParameterExtractor
{
    @Override
    public Map<String, Object> extract(final Map<String, Object> context)
    {
        if (context.containsKey("webInterfaceContext"))
        {
            WebInterfaceContext webInterfaceContext = (WebInterfaceContext) context.get("webInterfaceContext");
            if (null != webInterfaceContext && null != webInterfaceContext.getSpace())
            {
                return ImmutableMap.<String, Object>of("space", ImmutableMap.of(
                        "id", webInterfaceContext.getSpace().getId(),
                        "key", webInterfaceContext.getSpace().getKey()
                ));
            }
        }
        return Collections.emptyMap();
    }
}
