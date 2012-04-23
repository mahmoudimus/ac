package com.atlassian.labs.remoteapps.product.jira.webhook;

import com.atlassian.jira.event.JiraEvent;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.issue.Issue;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class IssueEventMapper extends JiraEventMapper
{
    @Override
    public boolean handles(JiraEvent event)
    {
        return event instanceof IssueEvent;
    }

    @Override
    public Map<String, Object> toMap(JiraEvent event)
    {
        IssueEvent issueEvent = (IssueEvent) event;

        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        builder.putAll(super.toMap(event));
        builder.put("user", issueEvent.getUser().getName());
        builder.put("issue", issueToMap(issueEvent.getIssue()));
        return builder.build();
    }

    private static Map<String, Object> issueToMap(Issue issue)
    {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        builder.put("key", issue.getKey());
        builder.put("summary", issue.getSummary());
        if (issue.getReporterUser() != null)
        {
            builder.put("reporterName", issue.getReporterUser().getName());
        }
        builder.put("status", issue.getStatusObject().getName());
        // TODO: Consider adding additional data about the issue

        return builder.build();
    }
}
