package com.atlassian.plugin.connect;

import com.atlassian.confluence.spaces.Space;
import com.atlassian.plugin.connect.spi.module.ContextParametersExtractor;
import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@Component
public final class TestConfluenceContextParametersExtractor implements ContextParametersExtractor
{
    @Override
    public Map<String, String> extractParameters(final Map<String, ? extends Object> context)
    {
        Object spaceObj = context.get("space");
        if (spaceObj != null && spaceObj instanceof Space)
        {
            Space space = (Space) spaceObj;
            return ImmutableMap.of(
                    "space.keyConcatId", space.getKey() + space.getId()
            );
        }
        else
        {
            return Collections.emptyMap();
        }
    }
}
