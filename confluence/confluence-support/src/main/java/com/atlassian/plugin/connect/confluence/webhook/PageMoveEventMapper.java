package com.atlassian.plugin.connect.confluence.webhook;

import java.util.Map;

import com.atlassian.confluence.event.events.ConfluenceEvent;
import com.atlassian.confluence.event.events.content.page.PageMoveEvent;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.sal.api.user.UserManager;

import com.google.common.collect.ImmutableMap;

public class PageMoveEventMapper extends ConfluenceEventMapper
{
    public PageMoveEventMapper(UserManager userManager, SettingsManager confluenceSettingsManager)
    {
        super(userManager, confluenceSettingsManager);
    }

    @Override
    public boolean handles(ConfluenceEvent e)
    {
        return e instanceof PageMoveEvent;
    }

    @Override
    public Map<String, Object> toMap(ConfluenceEvent e)
    {
        PageMoveEvent event = (PageMoveEvent) e;

        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        builder.putAll(super.toMap(e));

        builder.put("page", contentEntityObjectToMap(event.getPage()));
        builder.put("oldParent", contentEntityObjectToMap(event.getOldParentPage()));
        builder.put("newParent", contentEntityObjectToMap(event.getNewParentpage()));

        return builder.build();
    }
}
