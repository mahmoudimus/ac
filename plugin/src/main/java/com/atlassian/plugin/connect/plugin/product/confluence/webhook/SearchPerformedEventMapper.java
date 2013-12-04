package com.atlassian.plugin.connect.plugin.product.confluence.webhook;

import com.atlassian.confluence.event.events.ConfluenceEvent;
import com.atlassian.confluence.event.events.search.SearchPerformedEvent;
import com.atlassian.confluence.search.service.SpaceCategoryEnum;
import com.atlassian.confluence.search.v2.query.BooleanQuery;
import com.atlassian.confluence.search.v2.query.BoostingQuery;
import com.atlassian.confluence.search.v2.query.InSpaceQuery;
import com.atlassian.confluence.search.v2.query.SpaceCategoryQuery;
import com.atlassian.confluence.search.v2.query.TextFieldQuery;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

        // Note: don't call the base implementation because we want to populate the 'user' parameter differently.
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();

        builder.put("timestamp", event.getTimestamp());

        builder.put("results", event.getNumberOfResults());

        if (event.getUser() != null)
        {
            builder.put("user", event.getUser().getName());
        }

        builder.putAll(handleQueryParameters(event));

        return builder.build();
    }

    private Map<String, Object> handleQueryParameters(SearchPerformedEvent event)
    {
        Map<String, Object> parameterMap = new HashMap<String, Object>();

        // Unfortunately, there are many different cases when it comes to the query contents of the SearchPerformedEvent.
        // The below covers the originally handled case and the current behavior of confluence when searching from the UI.
        // However, this is brittle and should eventually be owned by Confluence.
        if (event.getSearchQuery() instanceof BoostingQuery)
        {
            String queryText = ((BoostingQuery) event.getSearchQuery()).getSearchQueryParameters().getQuery();
            parameterMap.put("query", queryText);
        }
        for (Object parameter : event.getSearchQuery().getParameters())
        {
            addSearchQueryParameter(parameter, parameterMap);
        }
        return parameterMap;
    }

    private void addSearchQueryParameter(Object query, Map<String, Object> parameters)
    {
        if (query instanceof TextFieldQuery)
        {
            String queryString = ((TextFieldQuery) query).getRawQuery();
            parameters.put("query", queryString);
        }
        else if (query instanceof InSpaceQuery)
        {
            List<String> spaceParameters = ((InSpaceQuery) query).getParameters();
            parameters.put("spaceKeys", ImmutableList.copyOf(spaceParameters));
        }
        else if (query instanceof SpaceCategoryQuery)
        {
            Set<SpaceCategoryEnum> spaceCategoryEnums = ((SpaceCategoryQuery) query).getSpaceCategories();
            Iterable<String> spaceCategories = Iterables.transform(spaceCategoryEnums, new Function<SpaceCategoryEnum, String>()
            {
                @Override
                public String apply(@Nullable SpaceCategoryEnum spaceCategoryEnum)
                {
                    return spaceCategoryEnum.getRepresentation();
                }
            });
            parameters.put("spaceCategories", ImmutableList.copyOf(spaceCategories));
        }
        else if (query instanceof BooleanQuery)
        {
            ((BooleanQuery) query).
        }
    }
}
