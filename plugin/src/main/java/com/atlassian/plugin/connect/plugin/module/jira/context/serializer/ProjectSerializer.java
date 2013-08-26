package com.atlassian.plugin.connect.plugin.module.jira.context.serializer;

import com.atlassian.jira.project.Project;
import com.atlassian.plugin.connect.plugin.module.context.ParameterSerializer;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Serializes Project objects.
 */
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
