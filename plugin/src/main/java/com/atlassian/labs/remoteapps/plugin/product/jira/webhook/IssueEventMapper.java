package com.atlassian.labs.remoteapps.plugin.product.jira.webhook;

import com.atlassian.jira.event.JiraEvent;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.labs.remoteapps.plugin.product.jira.JiraRestBeanMarshaler;
import com.google.common.collect.ImmutableMap;
import org.json.JSONException;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;

public class IssueEventMapper extends JiraEventMapper
{
    private static final Logger log = LoggerFactory.getLogger(IssueEventMapper.class);
    private final JiraRestBeanMarshaler jiraRestBeanMarshaler;

    public IssueEventMapper(JiraRestBeanMarshaler jiraRestBeanMarshaler)
    {
        this.jiraRestBeanMarshaler = jiraRestBeanMarshaler;
    }

    @Override
    public boolean handles(JiraEvent event)
    {
        return event instanceof IssueEvent;
    }

    @Override
    public Map<String, Object> toMap(JiraEvent event) throws JSONException
    {
        IssueEvent issueEvent = (IssueEvent) event;

        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        builder.putAll(super.toMap(event));
        if (issueEvent.getUser() != null)
        {
            builder.put("user", issueEvent.getUser().getName());
        }
        builder.put("issue", jiraRestBeanMarshaler.getRemoteIssue(issueEvent.getIssue()));

        if (EventType.ISSUE_UPDATED_ID.equals(issueEvent.getEventTypeId()))
        {
            builder.put("updatedFields", changeGroupToList(issueEvent.getChangeLog()));
        }
        if (issueEvent.getComment() != null)
        {
            builder.put("comment", jiraRestBeanMarshaler.getRemoteComment(issueEvent.getComment()));
        }
        return builder.build();
    }

    private List<Map<String, Object>> changeGroupToList(GenericValue changeLog)
    {
        List<Map<String, Object>> fields = newArrayList();
        try
        {
            for (GenericValue changeItem : changeLog.getRelated("ChildChangeItem"))
            {
                fields.add(ImmutableMap.<String, Object>of(
                        "name", changeItem.get("field").toString(),
                        "oldValue", getValueOrBlank(changeItem, "oldstring"),
                        "newValue", getValueOrBlank(changeItem, "newstring")));
            }
        }
        catch (GenericEntityException e)
        {
            log.warn("Error serializing updated event: "+e, e);
        }
        return fields;
    }

    private String getValueOrBlank(GenericValue gv, String name)
    {
        Object value = gv.get(name);
        return value != null ? value.toString() : "";
    }
}
