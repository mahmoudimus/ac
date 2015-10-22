package com.atlassian.plugin.connect.jira.iframe.context.serializer;

import com.atlassian.jira.project.Project;
import com.atlassian.plugin.connect.spi.module.context.ParameterSerializer;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Serializes Project objects.
 */
@JiraComponent
public class ProjectSerializer implements ParameterSerializer<Project>
{
    @Override
    public Map<String, Object> serialize(final Project project)
    {
        return ImmutableMap.<String, Object>of("project", ImmutableMap.of(
                "id", project.getId(),
                "key", project.getKey()
        ));
    }
}
