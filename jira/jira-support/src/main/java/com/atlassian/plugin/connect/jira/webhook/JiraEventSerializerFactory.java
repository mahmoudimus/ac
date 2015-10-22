package com.atlassian.plugin.connect.jira.webhook;

import java.util.List;

import com.atlassian.jira.event.JiraEvent;
import com.atlassian.plugin.connect.spi.product.EventMapper;
import com.atlassian.plugin.connect.jira.JiraRestBeanMarshaler;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.webhooks.spi.provider.EventSerializer;
import com.atlassian.webhooks.spi.provider.EventSerializerFactory;
import com.atlassian.webhooks.spi.provider.EventSerializers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Maps {@link com.atlassian.jira.event.JiraEvent} instances to {@link com.atlassian.webhooks.spi.provider.EventSerializer} instances so that the event information
 * can be transmitted via the WebHookPublisher.
 */
@JiraComponent
public final class JiraEventSerializerFactory implements EventSerializerFactory<JiraEvent>
{
    private static final Logger log = LoggerFactory.getLogger(JiraEventSerializerFactory.class);

    protected final List<EventMapper<JiraEvent>> mappers;

    @Autowired
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
                return EventSerializers.forMap(event, mapper.toMap(event));
            }
        }

        // This should never really happen; the mappers list has a default mapper within it that handles every type of event.
        log.warn(String.format("Event %s was not recognised by any Event to WebHook mapper.", event.getClass().getName()));
        return EventSerializers.forMap(event, ImmutableMap.<String, Object>builder().build());
    }
}
