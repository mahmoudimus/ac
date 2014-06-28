package com.atlassian.plugin.connect.plugin.module.confluence.context.extractor;

import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.plugin.connect.plugin.module.confluence.context.serializer.ContentSerializer;
import com.atlassian.plugin.connect.plugin.module.context.ContextMapParameterExtractor;
import com.atlassian.plugin.connect.plugin.module.context.ParameterSerializer;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.google.common.base.Optional;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@ConfluenceComponent
public class ContentContextMapParameterExtractor implements ContextMapParameterExtractor<ContentEntityObject>
{
    private static final String CONTENT_CONTEXT_PARAMETER = "content";

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
        }
        return Optional.absent();
    }

    @Override
    public ParameterSerializer<ContentEntityObject> serializer()
    {
        return contentSerializer;
    }
}
