package com.atlassian.labs.remoteapps.product.jira.webhook;

import com.atlassian.jira.event.JiraEvent;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.Comment;
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
        if (issueEvent.getUser() != null)
        {
            builder.put("user", issueEvent.getUser().getName());
        }
        builder.put("issue", issueToMap(issueEvent.getIssue()));

        if (EventType.ISSUE_UPDATED_ID.equals(issueEvent.getEventTypeId()))
        {
            builder.put("updatedFields", changeGroupToMap(issueEvent.getChangeLog()));
        }
        if (issueEvent.getComment() != null)
        {
            builder.put("comment", commentToMap(issueEvent.getComment()));
        }
        return builder.build();
    }

    private Map<String, Object> commentToMap(Comment comment)
    {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        builder.put("id", comment.getId());
        builder.put("body", comment.getBody());
        builder.put("author", comment.getAuthor());

        // TODO: Consider adding additional data about the issue

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
                        "oldValue", getValueOrBlank(changeItem, "oldstring"),
                        "newValue", getValueOrBlank(changeItem, "newstring")
                        ));
            }
        }
        catch (GenericEntityException e)
        {
            log.warn("Error serializing updated event: "+e, e);
        }
        return builder.build();
    }

    private String getValueOrBlank(GenericValue gv, String name)
    {
        Object value = gv.get(name);
        return value != null ? value.toString() : "";
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
