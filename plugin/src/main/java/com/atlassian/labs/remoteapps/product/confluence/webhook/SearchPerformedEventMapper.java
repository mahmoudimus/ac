package com.atlassian.labs.remoteapps.product.confluence.webhook;

import com.atlassian.confluence.event.events.ConfluenceEvent;
import com.atlassian.confluence.event.events.search.SearchPerformedEvent;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class SearchPerformedEventMapper extends ConfluenceEventMapper
{
    public SearchPerformedEventMapper(UserManager userManager, SettingsManager confluenceSettingsManager)
    {
        super(userManager, confluenceSettingsManager);
    }

    @Override
    public boolean handles(ConfluenceEvent e)
    {
        return e instanceof SearchPerformedEvent;
    }

    @Override
    public Map<String, Object> toMap(ConfluenceEvent e)
    {
        SearchPerformedEvent event = (SearchPerformedEvent) e;

        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        builder.putAll(super.toMap(event));
        builder.put("query", event.getSearchQuery().toString());
        builder.put("user", event.getUser().getName());
        builder.put("results", event.getNumberOfResults());

        return builder.build();
    }
}
