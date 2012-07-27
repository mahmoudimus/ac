package com.atlassian.labs.remoteapps.product.jira;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.jira.event.ProjectCreatedEvent;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.labs.remoteapps.product.jira.webhook.JiraEventSerializerFactory;
import com.atlassian.labs.remoteapps.webhook.external.EventMatcher;
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
        // may not ever be fired, seems to be treated as an update internally
        builder.put(EventType.ISSUE_ASSIGNED_ID, "issue_assigned");

        builder.put(EventType.ISSUE_CLOSED_ID, "issue_closed");
        builder.put(EventType.ISSUE_CREATED_ID, "issue_created");
        builder.put(EventType.ISSUE_REOPENED_ID, "issue_reopened");
        builder.put(EventType.ISSUE_RESOLVED_ID, "issue_resolved");
        builder.put(EventType.ISSUE_COMMENT_EDITED_ID, "issue_comment_edited");
        builder.put(EventType.ISSUE_COMMENTED_ID, "issue_commented");
        builder.put(EventType.ISSUE_DELETED_ID, "issue_deleted");
        builder.put(EventType.ISSUE_MOVED_ID, "issue_moved");
        builder.put(EventType.ISSUE_UPDATED_ID, "issue_updated");
        builder.put(EventType.ISSUE_WORKLOG_DELETED_ID, "issue_worklog_deleted");
        builder.put(EventType.ISSUE_WORKLOG_UPDATED_ID, "issue_worklog_updated");
        builder.put(EventType.ISSUE_WORKLOGGED_ID, "issue_work_logged");
        builder.put(EventType.ISSUE_WORKSTARTED_ID, "issue_work_started");
        builder.put(EventType.ISSUE_WORKSTOPPED_ID, "issue_work_stopped");
        builder.put(EventType.ISSUE_GENERICEVENT_ID, "issue_generic_event");

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
    
    private static final class EventTypeMatcher implements EventMatcher<IssueEvent>
    {
        private final Long eventType;

        private EventTypeMatcher(Long eventType)
        {
            this.eventType = eventType;
        }

        @Override
        public boolean matches(IssueEvent event, String pluginKey)
        {
            return eventType.equals(event.getEventTypeId());
        }
    }
    
}
