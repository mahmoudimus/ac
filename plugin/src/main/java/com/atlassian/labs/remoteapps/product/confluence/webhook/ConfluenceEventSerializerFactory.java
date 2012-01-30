package com.atlassian.labs.remoteapps.product.confluence.webhook;

import com.atlassian.confluence.event.events.ConfluenceEvent;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.labs.remoteapps.webhook.EventSerializer;
import com.atlassian.labs.remoteapps.webhook.MapEventSerializer;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Maps {@link ConfluenceEvent} instances to {@EventSerializer} instances so that the event information
 * can be transmitted via the {@link com.atlassian.labs.remoteapps.webhook.WebHookPublisher}.
 */
public class ConfluenceEventSerializerFactory
{
    private static final Logger log = LoggerFactory.getLogger(ConfluenceEventSerializerFactory.class);

    private final List<EventMapper> mappers;

    public ConfluenceEventSerializerFactory(UserManager userManager, SettingsManager confluenceSettingsManager)
    {
        // This list is deliberately ordered. More-specific mappers such as PageMoveEventMapper must appear in the
        // list _before_ less-specific mappers such as PageEventMapper, or else they will never get invoked.
        mappers = ImmutableList.<EventMapper>of(
                new LabelEventMapper(userManager, confluenceSettingsManager),
                new UserStatusEventMapper(userManager, confluenceSettingsManager),
                new SearchPerformedEventMapper(userManager, confluenceSettingsManager),
                new AttachmentEventMapper(userManager, confluenceSettingsManager),
                new PageChildrenReorderEventMapper(userManager, confluenceSettingsManager),
                new PageMoveEventMapper(userManager, confluenceSettingsManager),
                new PageEventMapper(userManager, confluenceSettingsManager),
                new BlogPostEventMapper(userManager, confluenceSettingsManager),
                new SpaceEventMapper(userManager, confluenceSettingsManager),
                new CommentEventMapper(userManager, confluenceSettingsManager),
                new SecurityEventMapper(userManager, confluenceSettingsManager),
                new ConfluenceEventMapper(userManager, confluenceSettingsManager)
        );
    }

    public EventSerializer getSerializer(ConfluenceEvent event)
    {
        for (EventMapper mapper : mappers)
        {
            if (mapper.handles(event))
                return new MapEventSerializer(event, mapper.toMap(event));
        }

        // This should never really happen; the mappers list has a default mapper within it that handles every type of ConfluenceEvent.
        log.warn(String.format("Event %s was not recognised by any Event to WebHook mapper.", event.getClass().getName()));
        return new MapEventSerializer(event, ImmutableMap.<String, Object>builder().build());
    }
}
