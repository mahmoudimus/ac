package com.atlassian.plugin.connect.plugin.module.jira.context.serializer;

import com.atlassian.jira.project.version.Version;
import com.atlassian.plugin.connect.plugin.module.context.ParameterSerializer;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Serializes Version objects.
 */
@JiraComponent
public class VersionSerializer implements ParameterSerializer<Version>
{
    @Override
    public Map<String, Object> serialize(final Version version)
    {
        return ImmutableMap.<String, Object>of("version", ImmutableMap.of("id", version.getId()));
    }
}
