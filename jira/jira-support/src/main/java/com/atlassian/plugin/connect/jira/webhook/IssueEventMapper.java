package com.atlassian.plugin.connect.jira.webhook;

import java.util.List;
import java.util.Map;

import com.atlassian.jira.event.JiraEvent;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.plugin.connect.jira.JiraRestBeanMarshaler;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

public final class IssueEventMapper extends JiraEventMapper
{
    private static final Logger log = LoggerFactory.getLogger(IssueEventMapper.class);
    private final JiraRestBeanMarshaler jiraRestBeanMarshaler;

    public IssueEventMapper(JiraRestBeanMarshaler jiraRestBeanMarshaler)
    {
        this.jiraRestBeanMarshaler = checkNotNull(jiraRestBeanMarshaler);
    }

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
        try
        {
            builder.put("issue", asMap(jiraRestBeanMarshaler.getRemoteIssue(issueEvent.getIssue())));
        }
        catch (JSONException e)
        {
            throw new RuntimeException(e);
        }

        if (EventType.ISSUE_UPDATED_ID.equals(issueEvent.getEventTypeId()) && issueEvent.getChangeLog() != null)
        {
            builder.put("updatedFields", changeGroupToList(issueEvent.getChangeLog()));
        }
        if (issueEvent.getComment() != null)
        {
            try
            {
                builder.put("comment", asMap(jiraRestBeanMarshaler.getRemoteComment(issueEvent.getComment())));
            }
            catch (JSONException e)
            {
                throw new RuntimeException(e);
            }
        }
        return builder.build();
    }

    private Object asMap(JSONObject object) throws JSONException
    {
        final ImmutableMap.Builder<String, Object> map = ImmutableMap.builder();

        if (object.length() > 0)
        {
            for (String name : JSONObject.getNames(object))
            {
                final Object value = object.get(name);
                if (value instanceof JSONObject)
                {
                    map.put(name, asMap((JSONObject) value));
                }
                else if (value instanceof JSONArray)
                {
                    map.put(name, asList((JSONArray) value));
                }
                else if (value instanceof JSONString)
                {
                    map.put(name, value.toString());
                }
                else
                {
                    map.put(name, value);
                }
            }
        }
        return map.build();
    }

    private List<Object> asList(JSONArray array) throws JSONException
    {
        final ImmutableList.Builder<Object> list = ImmutableList.builder();
        for (int i = 0; i < array.length(); i++)
        {
            final Object value = array.get(i);
            if (value instanceof JSONObject)
            {
                list.add(asMap((JSONObject) value));
            }
            else if (value instanceof JSONArray)
            {
                list.add(asList((JSONArray) value));
            }
            else if (value instanceof JSONString)
            {
                list.add(value.toString());
            }
            else
            {
                list.add(value);
            }
        }
        return list.build();
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
