package com.atlassian.plugin.connect.plugin.module.confluence.context.serializer;

import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.plugin.connect.plugin.module.context.ParameterSerializer;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Serializes page objects.
 */
@ConfluenceComponent
public class PageSerializer implements ParameterSerializer<AbstractPage>
{
    @Override
    public Map<String, Object> serialize(final AbstractPage page)
    {
        return ImmutableMap.<String, Object>of("page",
                ImmutableMap.of("id", page.getId())
        );
    }
}
