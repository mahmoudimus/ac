package com.atlassian.plugin.connect.plugin.product.confluence.webhook;

import java.util.Map;

import com.atlassian.confluence.event.events.ConfluenceEvent;
import com.atlassian.confluence.event.events.search.SearchPerformedEvent;
import com.atlassian.confluence.search.v2.query.BoostingQuery;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.sal.api.user.UserManager;

import com.google.common.collect.ImmutableMap;

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

        String queryText = "";
        // non-empircal testing indicates that BoostingQuery is always the top-level query used when searching Confluence.
        // unfortunately, because the SearchQuery interface is so sparse, the only way to easily get the original query
        // text is to cast to a specific query type. sad-face.
        if (event.getSearchQuery() instanceof BoostingQuery)
        {
            queryText = ((BoostingQuery)event.getSearchQuery()).getSearchQueryParameters().getQuery();
        }

        // Note: don't call the base implementation because we want to populate the 'user' parameter differently.
        ImmutableMap.Builder<String,Object> builder = ImmutableMap.builder();
        builder.put("timestamp", event.getTimestamp());
        builder.put("query", queryText);
        builder.put("results", event.getNumberOfResults());
        if (event.getUser() != null)
        {
            builder.put("user", event.getUser().getName());
        }
        return builder.build();
    }
}
