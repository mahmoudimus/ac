package com.atlassian.plugin.connect.plugin.product.confluence.webhook;

import com.atlassian.confluence.event.events.ConfluenceEvent;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.plugin.connect.plugin.product.EventMapper;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.webhooks.api.util.EventSerializers;
import com.atlassian.webhooks.spi.EventSerializer;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Maps {@link com.atlassian.confluence.event.events.ConfluenceEvent} instances to {@link
 * com.atlassian.webhooks.spi.EventSerializer} instances so that the event information can be transmitted via the
 * WebHookPublisher.
 */
@ConfluenceComponent
public final class ConfluenceEventSerializer implements EventSerializer<ConfluenceEvent>
{
    private static final Logger log = LoggerFactory.getLogger(ConfluenceEventSerializer.class);
    public static final EventSerializer<ConfluenceEvent> EMPTY_SERIALIZER = new EventSerializer<ConfluenceEvent>()
    {
        @Override
        public String serialize(final ConfluenceEvent event)
        {
            return "";
        }
    };

    private final List<EventMapper<ConfluenceEvent>> mappers;

    @Autowired
    public ConfluenceEventSerializer(UserManager userManager, SettingsManager confluenceSettingsManager)
    {
        // This list is deliberately ordered. More-specific mappers such as PageMoveEventMapper must appear in the
        // list _before_ less-specific mappers such as PageEventMapper, or else they will never get invoked.
        mappers = ImmutableList.<EventMapper<ConfluenceEvent>>of(
                new LabelEventMapper(userManager, confluenceSettingsManager),
                new UserStatusEventMapper(userManager, confluenceSettingsManager),
                new UserEventMapper(userManager, confluenceSettingsManager),
                new SearchPerformedEventMapper(userManager, confluenceSettingsManager),
                new AttachmentEventMapper(userManager, confluenceSettingsManager),
                new PageChildrenReorderEventMapper(userManager, confluenceSettingsManager),
                new PageMoveEventMapper(userManager, confluenceSettingsManager),
                new PageEventMapper(userManager, confluenceSettingsManager),
                new BlogPostEventMapper(userManager, confluenceSettingsManager),
                new SpaceEventMapper(userManager, confluenceSettingsManager),
                new CommentEventMapper(userManager, confluenceSettingsManager),
                new SecurityEventMapper(userManager, confluenceSettingsManager),
                new ContentEventMapper(userManager, confluenceSettingsManager),
                new ConfluenceEventMapper(userManager, confluenceSettingsManager)
        );
    }

    @Override
    public String serialize(final ConfluenceEvent event)
    {
        for (final EventMapper<ConfluenceEvent> mapper : mappers)
        {
            if (mapper.handles(event))
            {
                return EventSerializers.objectToJson(mapper.toMap(event));
            }
        }

        // This should never really happen; the mappers list has a default mapper within it that handles every type of ConfluenceEvent.
        log.warn(String.format("Event %s was not recognised by any Event to WebHook mapper.", event.getClass().getName()));
        return "{}";
    }
}
