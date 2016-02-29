package com.atlassian.plugin.connect.confluence.web.context;

import com.atlassian.confluence.spaces.Space;
import com.atlassian.plugin.connect.spi.web.context.ParameterSerializer;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Serializes space objects.
 */
@ConfluenceComponent
public class SpaceSerializer implements ParameterSerializer<Space> {
    @Override
    public Map<String, Object> serialize(Space space) {
        return ImmutableMap.<String, Object>of("space", ImmutableMap.of(
                "id", space.getId(),
                "key", space.getKey()
        ));
    }
}
