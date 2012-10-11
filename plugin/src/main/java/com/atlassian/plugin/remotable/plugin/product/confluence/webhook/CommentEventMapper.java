package com.atlassian.plugin.remotable.plugin.product.confluence.webhook;

import com.atlassian.confluence.event.events.ConfluenceEvent;
import com.atlassian.confluence.event.events.content.comment.CommentEvent;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class CommentEventMapper extends ConfluenceEventMapper
{
    public CommentEventMapper(UserManager userManager, SettingsManager confluenceSettingsManager)
    {
        super(userManager, confluenceSettingsManager);
    }

    @Override
    public boolean handles(ConfluenceEvent e)
    {
        return e instanceof CommentEvent;
    }

    @Override
    public Map<String, Object> toMap(ConfluenceEvent e)
    {
        CommentEvent event = (CommentEvent) e;

        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        builder.putAll(super.toMap(e));
        builder.put("comment", commentToMap(event.getComment()));
        return builder.build();
    }
}

