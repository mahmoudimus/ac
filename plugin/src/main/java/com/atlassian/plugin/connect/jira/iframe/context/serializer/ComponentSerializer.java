package com.atlassian.plugin.connect.jira.iframe.context.serializer;

import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.plugin.connect.spi.module.context.ParameterSerializer;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Serializes ProjectComponent objects.
 */
@JiraComponent
public class ComponentSerializer implements ParameterSerializer<ProjectComponent>
{
    @Override
    public Map<String, Object> serialize(final ProjectComponent projectComponent)
    {
        return ImmutableMap.<String, Object>of("component",
                ImmutableMap.of("id", projectComponent.getId()));
    }
}
