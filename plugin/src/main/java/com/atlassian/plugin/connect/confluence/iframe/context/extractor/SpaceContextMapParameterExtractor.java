package com.atlassian.plugin.connect.confluence.iframe.context.extractor;

import com.atlassian.confluence.pages.actions.AbstractPageAwareAction;
import com.atlassian.confluence.plugin.descriptor.web.WebInterfaceContext;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.plugin.connect.confluence.iframe.context.serializer.SpaceSerializer;
import com.atlassian.plugin.connect.plugin.module.context.ContextMapParameterExtractor;
import com.atlassian.plugin.connect.plugin.module.context.ParameterSerializer;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;

import com.google.common.base.Optional;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Extracts space parameters that can be included in webpanel's iframe url.
 */
@ConfluenceComponent
public class SpaceContextMapParameterExtractor implements ContextMapParameterExtractor<Space>
{
    private static final String SPACE_CONTEXT_PARAMETER = "space";
    private static final String ACTION_PARAMETER = "action";
    private SpaceSerializer spaceSerializer;

    @Autowired
    public SpaceContextMapParameterExtractor(SpaceSerializer spaceSerializer)
    {
        this.spaceSerializer = spaceSerializer;
    }

    @Override
    public Optional<Space> extract(final Map<String, Object> context)
    {
        if (context.containsKey("webInterfaceContext"))
        {
            WebInterfaceContext webInterfaceContext = (WebInterfaceContext) context.get("webInterfaceContext");
            if (null != webInterfaceContext && null != webInterfaceContext.getSpace())
            {
                return Optional.of(webInterfaceContext.getSpace());
            }
        }
        else if (context.containsKey(SPACE_CONTEXT_PARAMETER) && context.get(SPACE_CONTEXT_PARAMETER) instanceof Space)
        {
            return Optional.of((Space) context.get(SPACE_CONTEXT_PARAMETER));
        }
        else if (context.containsKey(ACTION_PARAMETER) && context.get(ACTION_PARAMETER) instanceof AbstractPageAwareAction)
        {
            AbstractPageAwareAction action = (AbstractPageAwareAction) context.get(ACTION_PARAMETER);
            return Optional.fromNullable(action.getSpace());
        }
        return Optional.absent();
    }

    @Override
    public ParameterSerializer<Space> serializer()
    {
        return spaceSerializer;
    }
}
