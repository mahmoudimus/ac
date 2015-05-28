package com.atlassian.plugin.connect.confluence.iframe.context.extractor;

import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.confluence.pages.actions.AbstractPageAwareAction;
import com.atlassian.confluence.plugin.descriptor.web.WebInterfaceContext;
import com.atlassian.plugin.connect.confluence.iframe.context.serializer.ContentSerializer;
import com.atlassian.plugin.connect.spi.module.context.ContextMapParameterExtractor;
import com.atlassian.plugin.connect.spi.module.context.ParameterSerializer;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.google.common.base.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@ConfluenceComponent
public class ContentContextMapParameterExtractor implements ContextMapParameterExtractor<ContentEntityObject>
{
    private static final Logger LOG = LoggerFactory.getLogger(ContentContextMapParameterExtractor.class);

    public static final String CONTENT_CONTEXT_PARAMETER = "content";
    public static final String PAGE_CONTEXT_PARAMETER = "page";
    public static final String ACTION_PARAMETER = "action";
    public static final String WEB_INTERFACE_CONTEXT_PARAMETER = "webInterfaceContext";

    private final ContentSerializer contentSerializer;

    @Autowired
    public ContentContextMapParameterExtractor(ContentSerializer contentSerializer)
    {
        this.contentSerializer = contentSerializer;
    }

    @Override
    public Optional<ContentEntityObject> extract(Map<String, Object> context)
    {
        if (context.containsKey(CONTENT_CONTEXT_PARAMETER))
        {
            Object contentFromContext = context.get(CONTENT_CONTEXT_PARAMETER);
            if (contentFromContext instanceof ContentEntityObject)
            {
                return Optional.of((ContentEntityObject)contentFromContext);
            }
            else
            {
                LOG.debug("Encountered a 'content' context parameter that is a " + contentFromContext.getClass().getName() + " rather than a ContentEntityObject. Skipping.");
            }
        }
        else if (context.containsKey(WEB_INTERFACE_CONTEXT_PARAMETER))
        {
            WebInterfaceContext webInterfaceContext = (WebInterfaceContext) context.get(WEB_INTERFACE_CONTEXT_PARAMETER);
            if (null != webInterfaceContext && null != webInterfaceContext.getPage())
            {
                return Optional.<ContentEntityObject>of(webInterfaceContext.getPage());

            }
        }
        else if (context.containsKey(PAGE_CONTEXT_PARAMETER) && context.get(PAGE_CONTEXT_PARAMETER) instanceof AbstractPage)
        {
            return Optional.of((ContentEntityObject) context.get(PAGE_CONTEXT_PARAMETER));
        }
        else if (context.containsKey(ACTION_PARAMETER) && context.get(ACTION_PARAMETER) instanceof AbstractPageAwareAction)
        {
            AbstractPageAwareAction action = (AbstractPageAwareAction) context.get(ACTION_PARAMETER);
            return Optional.<ContentEntityObject>fromNullable(action.getPage());
        }
        return Optional.absent();
    }

    @Override
    public ParameterSerializer<ContentEntityObject> serializer()
    {
        return contentSerializer;
    }
}
