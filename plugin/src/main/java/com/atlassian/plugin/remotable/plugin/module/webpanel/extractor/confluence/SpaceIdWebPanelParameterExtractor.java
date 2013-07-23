package com.atlassian.plugin.remotable.plugin.module.webpanel.extractor.confluence;

import com.atlassian.confluence.plugin.descriptor.web.DefaultWebInterfaceContext;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.plugin.remotable.plugin.module.webpanel.extractor.WebPanelParameterExtractor;
import com.google.common.base.Function;
import com.google.common.base.Optional;

import java.util.Map;

/**
 * Extracts space id that will be included in webpanel's iframe url.
 */
public class SpaceIdWebPanelParameterExtractor implements WebPanelParameterExtractor
{
    public static final String SPACE_ID = "space_id";

    @Override
    public Optional<Map.Entry<String, String[]>> extract(final Map<String, Object> context)
    {
        return WebInterfaceContextExtractor.extractFromWebInterfaceContext(context, new Function<DefaultWebInterfaceContext, Map.Entry<String, String[]>>()
        {
            @Override
            public Map.Entry<String, String[]> apply(final DefaultWebInterfaceContext context)
            {
                final Space space = context.getSpace();
                return space != null ? new ImmutableWebPanelParameterPair(SPACE_ID, new String[] { String.valueOf(space.getId()) }) : null;
            }
        });
    }
}
