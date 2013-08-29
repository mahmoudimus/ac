package com.atlassian.plugin.connect.plugin.module.confluence.context.serializer;

import com.atlassian.confluence.spaces.Space;
import com.atlassian.plugin.connect.plugin.module.context.ParameterSerializer;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Serializes space objects.
 */
public class SpaceSerializer implements ParameterSerializer<Space>
{
    @Override
    public Map<String, Object> serialize(Space space)
    {
        return ImmutableMap.<String, Object>of("space", ImmutableMap.of(
                "id", space.getId(),
                "key", space.getKey()
        ));
    }
}
