package com.atlassian.labs.remoteapps.product.confluence.webhook;

import com.atlassian.confluence.event.events.ConfluenceEvent;
import com.atlassian.confluence.event.events.security.SecurityEvent;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class SecurityEventMapper extends ConfluenceEventMapper
{
    public SecurityEventMapper(UserManager userManager, SettingsManager confluenceSettingsManager)
    {
        super(userManager, confluenceSettingsManager);
    }

    @Override
    public boolean handles(ConfluenceEvent e)
    {
        return e instanceof SecurityEvent;
    }

    @Override
    public Map<String, Object> toMap(ConfluenceEvent e)
    {
        final SecurityEvent event = (SecurityEvent) e;

        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();

        // Note: don't call the base implementation because we want to populate the 'user' parameter differently.
        builder.put("timestamp", e.getTimestamp());
        builder.put("user", event.getUsername());

        return builder.build();
    }
}
