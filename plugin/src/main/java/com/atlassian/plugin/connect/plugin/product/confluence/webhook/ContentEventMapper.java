package com.atlassian.plugin.connect.plugin.product.confluence.webhook;

import com.atlassian.confluence.event.events.ConfluenceEvent;
import com.atlassian.confluence.event.events.content.ContentEvent;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class ContentEventMapper extends ConfluenceEventMapper
{
    public ContentEventMapper(UserManager userManager, SettingsManager confluenceSettingsManager)
    {
        super(userManager, confluenceSettingsManager);
    }

    @Override
    public boolean handles(ConfluenceEvent e)
    {
        return e instanceof ContentEvent;
    }

    @Override
    public Map<String, Object> toMap(ConfluenceEvent event)
    {
        ContentEvent contentEvent = (ContentEvent) event;

        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        builder.putAll(super.toMap(event));
        builder.put("content", contentEntityObjectToMap(contentEvent.getContent()));
        return builder.build();
    }
}
