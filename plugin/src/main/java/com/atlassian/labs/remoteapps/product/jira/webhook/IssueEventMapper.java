package com.atlassian.labs.remoteapps.product.jira.webhook;

import com.atlassian.jira.event.JiraEvent;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import com.google.common.collect.ImmutableMap;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class IssueEventMapper extends JiraEventMapper
{
    private static final Logger log = LoggerFactory.getLogger(IssueEventMapper.class);
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

        if (EventType.ISSUE_UPDATED_ID.equals(issueEvent.getEventTypeId()))
        {
            builder.put("updatedFields", changeGroupToMap(issueEvent.getChangeLog()));
        }
        return builder.build();
    }

    private Map<String, Object> changeGroupToMap(GenericValue changeLog)
    {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        try
        {
            for (GenericValue changeItem : changeLog.getRelated("ChildChangeItem"))
            {
                builder.put(changeItem.get("field").toString(), ImmutableMap.of(
                        "oldValue", changeItem.get("oldstring"),
                        "newValue", changeItem.get("newstring")
                        ));
            }
        }
        catch (GenericEntityException e)
        {
            log.warn("Error serializing updated event: "+e, e);
        }
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
