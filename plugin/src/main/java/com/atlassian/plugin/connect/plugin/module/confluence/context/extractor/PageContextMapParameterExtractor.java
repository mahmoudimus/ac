package com.atlassian.plugin.connect.plugin.module.confluence.context.extractor;

import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.confluence.plugin.descriptor.web.WebInterfaceContext;
import com.atlassian.plugin.connect.plugin.module.confluence.context.serializer.PageSerializer;
import com.atlassian.plugin.connect.plugin.module.context.ParameterSerializer;
import com.atlassian.plugin.connect.plugin.module.context.ContextMapParameterExtractor;
import com.google.common.base.Optional;

import java.util.Map;

/**
 * Extracts page parameters that can be included in webpanel's iframe url.
 */
public class PageContextMapParameterExtractor implements ContextMapParameterExtractor<AbstractPage>
{
    private static final String PAGE_CONTEXT_PARAMETER = "page";
    private final PageSerializer pageSerializer;

    public PageContextMapParameterExtractor(PageSerializer pageSerializer)
    {
        this.pageSerializer = pageSerializer;
    }


    @Override
    public Optional<AbstractPage> extract(final Map<String, Object> context)
    {
        if (context.containsKey("webInterfaceContext"))
        {
            WebInterfaceContext webInterfaceContext = (WebInterfaceContext) context.get("webInterfaceContext");
            if (null != webInterfaceContext && null != webInterfaceContext.getPage())
            {
                return Optional.of(webInterfaceContext.getPage());

            }
        }
        else if (context.containsKey(PAGE_CONTEXT_PARAMETER) && context.get(PAGE_CONTEXT_PARAMETER) instanceof AbstractPage)
        {
            return Optional.of((AbstractPage) context.get(PAGE_CONTEXT_PARAMETER));
        }
        return Optional.absent();
    }

    @Override
    public ParameterSerializer<AbstractPage> serializer()
    {
        return pageSerializer;
    }
}
