package com.atlassian.plugin.connect.confluence.web.context;

import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.confluence.pages.actions.AbstractPageAwareAction;
import com.atlassian.confluence.plugin.descriptor.web.WebInterfaceContext;
import com.atlassian.plugin.connect.spi.web.context.ParameterSerializer;
import com.atlassian.plugin.connect.spi.web.context.ContextMapParameterExtractor;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Extracts page parameters that can be included in webpanel's iframe url.
 *
 * The context parameters produced by this extractor are considered deprecated.
 * @see ContentContextMapParameterExtractor
 */
@ConfluenceComponent
public class PageContextMapParameterExtractor implements ContextMapParameterExtractor<AbstractPage>
{
    private static final String PAGE_CONTEXT_PARAMETER = "page";
    private static final String ACTION_PARAMETER = "action";
    private final PageSerializer pageSerializer;

    @Autowired
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
        else if (context.containsKey(ACTION_PARAMETER) && context.get(ACTION_PARAMETER) instanceof AbstractPageAwareAction)
        {
            AbstractPageAwareAction action = (AbstractPageAwareAction) context.get(ACTION_PARAMETER);
            return Optional.ofNullable(action.getPage());
        }
        return Optional.empty();
    }

    @Override
    public ParameterSerializer<AbstractPage> serializer()
    {
        return pageSerializer;
    }
}
