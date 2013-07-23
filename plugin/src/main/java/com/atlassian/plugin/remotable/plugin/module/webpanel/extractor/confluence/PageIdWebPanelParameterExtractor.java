package com.atlassian.plugin.remotable.plugin.module.webpanel.extractor.confluence;

import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.confluence.plugin.descriptor.web.DefaultWebInterfaceContext;
import com.atlassian.plugin.remotable.plugin.module.webpanel.extractor.WebPanelParameterExtractor;
import com.google.common.base.Function;
import com.google.common.base.Optional;

import java.util.Map;

/**
 * Extracts page id that will be included in webpanel's iframe url.
 */
public class PageIdWebPanelParameterExtractor implements WebPanelParameterExtractor
{
    public static final String PAGE_ID = "page_id";

    @Override
    public Optional<Map.Entry<String, String[]>> extract(final Map<String, Object> context)
    {
        return WebInterfaceContextExtractor.extractFromWebInterfaceContext(context, new Function<DefaultWebInterfaceContext, Map.Entry<String, String[]>>()
        {
            @Override
            public Map.Entry<String, String[]> apply(final DefaultWebInterfaceContext context)
            {
                final AbstractPage page = context.getPage();
                return page != null ? new ImmutableWebPanelParameterPair(PAGE_ID, new String[] { String.valueOf(page.getId()) }) : null;
            }
        });
    }
}
