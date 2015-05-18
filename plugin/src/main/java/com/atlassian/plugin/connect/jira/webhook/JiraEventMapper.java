package com.atlassian.plugin.connect.jira.webhook;

import java.util.Map;

import com.atlassian.jira.event.JiraEvent;
import com.atlassian.plugin.connect.plugin.product.EventMapper;

import com.google.common.collect.ImmutableMap;

public class JiraEventMapper implements EventMapper<JiraEvent>
{
    @Override
    public boolean handles(JiraEvent event)
    {
        return false;
    }

    @Override
    public Map<String, Object> toMap(JiraEvent event)
    {
        return ImmutableMap.<String, Object>of(
                "timestamp", event.getTime().getTime()
        );
    }
}
