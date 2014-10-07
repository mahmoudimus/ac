package com.atlassian.plugin.connect.plugin.product.jira.webhook;

import com.atlassian.jira.event.JiraEvent;
import com.atlassian.plugin.connect.plugin.product.EventMapper;
import com.atlassian.plugin.connect.plugin.product.jira.JiraRestBeanMarshaler;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.webhooks.api.util.EventSerializers;
import com.atlassian.webhooks.spi.EventSerializer;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Maps {@link com.atlassian.jira.event.JiraEvent} instances to {@link com.atlassian.webhooks.spi.EventSerializer}
 * instances so that the event information can be transmitted via the WebHookPublisher.
 */
@JiraComponent
public final class JiraEventSerializerFactory implements EventSerializer<JiraEvent>
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
    public String serialize(final JiraEvent event)
    {
        for (EventMapper<JiraEvent> mapper : mappers)
        {
            if (mapper.handles(event))
            {
                return EventSerializers.objectToJson(mapper.toMap(event));
            }
        }

        // This should never really happen; the mappers list has a default mapper within it that handles every type of event.
        log.warn(String.format("Event %s was not recognised by any Event to WebHook mapper.", event.getClass().getName()));
        return "{}";
    }
}
