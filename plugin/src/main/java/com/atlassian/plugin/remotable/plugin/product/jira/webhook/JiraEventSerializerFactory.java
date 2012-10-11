package com.atlassian.plugin.remotable.plugin.product.jira.webhook;

import com.atlassian.jira.event.JiraEvent;
import com.atlassian.plugin.remotable.plugin.product.EventMapper;
import com.atlassian.plugin.remotable.plugin.product.jira.JiraRestBeanMarshaler;
import com.atlassian.plugin.remotable.plugin.webhook.MapEventSerializer;
import com.atlassian.plugin.remotable.spi.webhook.EventSerializer;
import com.atlassian.plugin.remotable.spi.webhook.EventSerializerFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.google.common.base.Preconditions.*;

/**
 * Maps {@link JiraEvent} instances to {@link EventSerializer} instances so that the event information
 * can be transmitted via the {@link com.atlassian.plugin.remotable.plugin.webhook.WebHookPublisherImpl}.
 */
public final class JiraEventSerializerFactory implements EventSerializerFactory<JiraEvent>
{
    private static final Logger log = LoggerFactory.getLogger(JiraEventSerializerFactory.class);

    protected final List<EventMapper<JiraEvent>> mappers;

    public JiraEventSerializerFactory(JiraRestBeanMarshaler jiraRestBeanMarshaler)
    {
        // This list is deliberately ordered. More-specific mappers must appear in the
        // list _before_ less-specific mappers, or else they will never get invoked.
        this.mappers = ImmutableList.<EventMapper<JiraEvent>>of(new IssueEventMapper(checkNotNull(jiraRestBeanMarshaler)));
    }

    @Override
    public EventSerializer create(JiraEvent event)
    {
        for (EventMapper<JiraEvent> mapper : mappers)
        {
            if (mapper.handles(event))
            {
                return new MapEventSerializer(event, mapper.toMap(event));
            }
        }

        // This should never really happen; the mappers list has a default mapper within it that handles every type of event.
        log.warn(String.format("Event %s was not recognised by any Event to WebHook mapper.", event.getClass().getName()));
        return new MapEventSerializer(event, ImmutableMap.<String, Object>builder().build());
    }
}
