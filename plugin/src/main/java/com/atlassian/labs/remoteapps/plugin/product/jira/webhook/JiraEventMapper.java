package com.atlassian.labs.remoteapps.plugin.product.jira.webhook;

import com.atlassian.jira.event.JiraEvent;
import com.atlassian.labs.remoteapps.plugin.product.EventMapper;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

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
