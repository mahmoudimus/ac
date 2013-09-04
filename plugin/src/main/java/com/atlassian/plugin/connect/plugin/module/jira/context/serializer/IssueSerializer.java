package com.atlassian.plugin.connect.plugin.module.jira.context.serializer;

import com.atlassian.jira.issue.Issue;
import com.atlassian.plugin.connect.plugin.module.context.ParameterSerializer;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Serializes Issue objects.
 */
public class IssueSerializer implements ParameterSerializer<Issue>
{
    @Override
    public Map<String, Object> serialize(final Issue issue)
    {
        return ImmutableMap.<String, Object>of("issue", ImmutableMap.of(
                "id", issue.getId(),
                "key", issue.getKey()
        ));
    }
}
