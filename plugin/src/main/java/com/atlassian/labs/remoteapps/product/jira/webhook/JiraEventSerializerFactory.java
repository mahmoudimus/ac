package com.atlassian.labs.remoteapps.product.jira.webhook;

import com.atlassian.jira.event.JiraEvent;
import com.atlassian.labs.remoteapps.product.EventMapper;
import com.atlassian.labs.remoteapps.webhook.external.EventSerializer;
import com.atlassian.labs.remoteapps.webhook.MapEventSerializer;
import com.atlassian.labs.remoteapps.webhook.external.EventSerializerFactory;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Maps {@link JiraEvent} instances to {@EventSerializer} instances so that the event information
 * can be transmitted via the {@link com.atlassian.labs.remoteapps.webhook.WebHookPublisher}.
 */
public class JiraEventSerializerFactory implements EventSerializerFactory<JiraEvent>
{
    private static final Logger log = LoggerFactory.getLogger(JiraEventSerializerFactory.class);

    protected final List<EventMapper<JiraEvent>> mappers;

    public JiraEventSerializerFactory(UserManager userManager)
    {
        // This list is deliberately ordered. More-specific mappers must appear in the
        // list _before_ less-specific mappers, or else they will never get invoked.
        this.mappers = ImmutableList.<EventMapper<JiraEvent>>of(
                new IssueEventMapper()
        );
    }

    @Override
    public EventSerializer create(JiraEvent event)
    {
        for (EventMapper<JiraEvent> mapper : mappers)
        {
            if (mapper.handles(event))
                return new MapEventSerializer(event, mapper.toMap(event));
        }

        // This should never really happen; the mappers list has a default mapper within it that handles every type of event.
        log.warn(String.format("Event %s was not recognised by any Event to WebHook mapper.", event.getClass().getName()));
        return new MapEventSerializer(event, ImmutableMap.<String, Object>builder().build());
    }
}
