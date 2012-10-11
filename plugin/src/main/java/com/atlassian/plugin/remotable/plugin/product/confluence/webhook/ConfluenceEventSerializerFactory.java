package com.atlassian.plugin.remotable.plugin.product.confluence.webhook;

import com.atlassian.confluence.event.events.ConfluenceEvent;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.plugin.remotable.plugin.product.EventMapper;
import com.atlassian.plugin.remotable.plugin.webhook.MapEventSerializer;
import com.atlassian.plugin.remotable.spi.webhook.EventSerializer;
import com.atlassian.plugin.remotable.spi.webhook.EventSerializerFactory;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Maps {@link ConfluenceEvent} instances to {@link EventSerializer} instances so that the event information
 * can be transmitted via the {@link com.atlassian.plugin.remotable.plugin.webhook.WebHookPublisherImpl}.
 */
public final class ConfluenceEventSerializerFactory implements EventSerializerFactory<ConfluenceEvent>
{
    private static final Logger log = LoggerFactory.getLogger(ConfluenceEventSerializerFactory.class);

    private final List<EventMapper<ConfluenceEvent>> mappers;

    public ConfluenceEventSerializerFactory(UserManager userManager, SettingsManager confluenceSettingsManager)
    {
        // This list is deliberately ordered. More-specific mappers such as PageMoveEventMapper must appear in the
        // list _before_ less-specific mappers such as PageEventMapper, or else they will never get invoked.
        mappers = ImmutableList.<EventMapper<ConfluenceEvent>>of(
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

    @Override
    public EventSerializer create(ConfluenceEvent event)
    {
        for (EventMapper<ConfluenceEvent> mapper : mappers)
        {
            if (mapper.handles(event))
            {
                return new MapEventSerializer(event, mapper.toMap(event));
            }
        }

        // This should never really happen; the mappers list has a default mapper within it that handles every type of ConfluenceEvent.
        log.warn(String.format("Event %s was not recognised by any Event to WebHook mapper.", event.getClass().getName()));
        return new MapEventSerializer(event, ImmutableMap.<String, Object>builder().build());
    }
}
