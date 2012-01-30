package com.atlassian.labs.remoteapps.product.confluence.webhook;

import com.atlassian.confluence.event.events.ConfluenceEvent;
import com.atlassian.confluence.event.events.userstatus.AbstractStatusContentEvent;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class UserStatusEventMapper extends ConfluenceEventMapper
{
    public UserStatusEventMapper(UserManager userManager, SettingsManager confluenceSettingsManager)
    {
        super(userManager, confluenceSettingsManager);
    }

    @Override
    public boolean handles(ConfluenceEvent e)
    {
        return e instanceof AbstractStatusContentEvent;
    }

    @Override
    public Map<String, Object> toMap(ConfluenceEvent e)
    {
        AbstractStatusContentEvent event = (AbstractStatusContentEvent) e;

        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();

        builder.put("status", event.getUserStatus().getBodyAsString());

        return builder.build();
    }
}
