package com.atlassian.plugin.connect.confluence.webhook;

import java.util.Map;

import com.atlassian.confluence.event.events.ConfluenceEvent;
import com.atlassian.confluence.event.events.space.SpaceEvent;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.sal.api.user.UserManager;

import com.google.common.collect.ImmutableMap;

public class SpaceEventMapper extends ConfluenceEventMapper
{
    public SpaceEventMapper(UserManager userManager, SettingsManager confluenceSettingsManager)
    {
        super(userManager, confluenceSettingsManager);
    }

    @Override
    public boolean handles(ConfluenceEvent e)
    {
        return e instanceof SpaceEvent;
    }

    @Override
    public Map<String, Object> toMap(ConfluenceEvent e)
    {
        SpaceEvent event = (SpaceEvent) e;

        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        builder.putAll(super.toMap(event));
        if (event.getSpace() != null)
            builder.put("space", spaceToMap(event.getSpace()));

        return builder.build();
    }
}
