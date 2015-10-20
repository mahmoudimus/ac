package com.atlassian.plugin.connect.jira.iframe.context.serializer;

import com.atlassian.jira.issue.Issue;
import com.atlassian.plugin.connect.spi.module.context.ParameterSerializer;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Serializes Issue objects.
 */
@JiraComponent
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
