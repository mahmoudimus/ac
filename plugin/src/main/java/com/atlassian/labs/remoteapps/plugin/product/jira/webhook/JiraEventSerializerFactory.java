package com.atlassian.labs.remoteapps.plugin.product.jira.webhook;

import com.atlassian.jira.event.JiraEvent;
import com.atlassian.labs.remoteapps.plugin.product.EventMapper;
import com.atlassian.labs.remoteapps.plugin.product.jira.JiraRestBeanMarshaler;
import com.atlassian.labs.remoteapps.spi.webhook.EventSerializer;
import com.atlassian.labs.remoteapps.plugin.webhook.MapEventSerializer;
import com.atlassian.labs.remoteapps.spi.webhook.EventSerializerFactory;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Maps {@link JiraEvent} instances to {@EventSerializer} instances so that the event information
 * can be transmitted via the {@link com.atlassian.labs.remoteapps.plugin.webhook.WebHookPublisher}.
 */
public class JiraEventSerializerFactory implements EventSerializerFactory<JiraEvent>
{
    private static final Logger log = LoggerFactory.getLogger(JiraEventSerializerFactory.class);
    private final JiraRestBeanMarshaler jiraRestBeanMarshaler;

    protected final List<EventMapper<JiraEvent>> mappers;

    public JiraEventSerializerFactory(JiraRestBeanMarshaler jiraRestBeanMarshaler)
    {
        this.jiraRestBeanMarshaler = jiraRestBeanMarshaler;
        // This list is deliberately ordered. More-specific mappers must appear in the
        // list _before_ less-specific mappers, or else they will never get invoked.
        this.mappers = ImmutableList.<EventMapper<JiraEvent>>of(
                new IssueEventMapper(this.jiraRestBeanMarshaler)
        );
    }

    @Override
    public EventSerializer create(JiraEvent event)
    {
        for (EventMapper<JiraEvent> mapper : mappers)
        {
            if (mapper.handles(event))
                try
                {
                    return new MapEventSerializer(event, mapper.toMap(event));
                }
                catch (JSONException e)
                {
                    throw new IllegalArgumentException(e);
                }
        }

        // This should never really happen; the mappers list has a default mapper within it that handles every type of event.
        log.warn(String.format("Event %s was not recognised by any Event to WebHook mapper.", event.getClass().getName()));
        return new MapEventSerializer(event, ImmutableMap.<String, Object>builder().build());
    }
}
