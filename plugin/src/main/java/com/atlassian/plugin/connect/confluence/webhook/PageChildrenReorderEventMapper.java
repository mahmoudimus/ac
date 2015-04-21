package com.atlassian.plugin.connect.confluence.webhook;

import java.util.Map;

import com.atlassian.confluence.event.events.ConfluenceEvent;
import com.atlassian.confluence.event.events.content.page.PageChildrenReorderEvent;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.sal.api.user.UserManager;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

public class PageChildrenReorderEventMapper extends ConfluenceEventMapper
{
    public PageChildrenReorderEventMapper(UserManager userManager, SettingsManager confluenceSettingsManager)
    {
        super(userManager, confluenceSettingsManager);
    }

    @Override
    public boolean handles(ConfluenceEvent e)
    {
        return e instanceof PageChildrenReorderEvent;
    }

    @Override
    public Map<String, Object> toMap(ConfluenceEvent e)
    {
        PageChildrenReorderEvent event = (PageChildrenReorderEvent) e;

        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        builder.putAll(super.toMap(e));
        builder.put("page", contentEntityObjectToMap(event.getPage()));

        final Function<Page, Map<String, Object>> toMap = new Function<Page, Map<String, Object>>()
        {
            @Override
            public Map<String, Object> apply(Page page)
            {
                return contentEntityObjectToMap(page);
            }
        };

        builder.put("oldSortedChildren", Lists.<Page, Map<String, Object>>transform(event.getOldSortedChildren(), toMap));
        builder.put("newSortedChildren", Lists.<Page, Map<String, Object>>transform(event.getNewSortedChildren(), toMap));

        return builder.build();
    }
}