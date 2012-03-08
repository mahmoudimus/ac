package com.atlassian.labs.remoteapps.product.jira;

import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.labs.remoteapps.product.jira.webhook.JiraEventSerializerFactory;
import com.atlassian.labs.remoteapps.webhook.external.WebHookProvider;
import com.atlassian.labs.remoteapps.webhook.external.WebHookRegistrar;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class JiraWebHookProvider implements WebHookProvider
{
    private final JiraEventSerializerFactory eventSerializerFactory;

    public JiraWebHookProvider(JiraEventSerializerFactory eventSerializerFactory)
    {
        this.eventSerializerFactory = eventSerializerFactory;
    }

    private Map<Long, String> buildIdentifierByEventTypeMap()
    {
        ImmutableMap.Builder<Long, String> builder = ImmutableMap.builder();
        builder.put(EventType.ISSUE_ASSIGNED_ID, "issue_assigned");
        builder.put(EventType.ISSUE_CLOSED_ID, "issue_closed");
        builder.put(EventType.ISSUE_CREATED_ID, "issue_created");
        builder.put(EventType.ISSUE_REOPENED_ID, "issue_reopened");
        builder.put(EventType.ISSUE_RESOLVED_ID, "issue_resolved");
        return builder.build();
    }

    @Override
    public void provide(WebHookRegistrar publish)
    {
        for (Map.Entry<Long,String> entry : buildIdentifierByEventTypeMap().entrySet())
        {
            publish.webhook(entry.getValue()).whenFired(IssueEvent.class).matchedBy(
                    new EventTypeMatcher(entry.getKey())).serializedWith(eventSerializerFactory);

        }
    }
    
    private static final class EventTypeMatcher implements Predicate<IssueEvent>
    {
        private final Long eventType;

        private EventTypeMatcher(Long eventType)
        {
            this.eventType = eventType;
        }

        @Override
        public boolean apply(IssueEvent input)
        {
            return eventType.equals(input.getEventTypeId());
        }
    }
    
}
