package com.atlassian.labs.remoteapps.plugin.product.confluence;

import static java.util.Arrays.asList;

/**
 * API Scope for Confluence that grants Remote Apps the ability to vuew and modify notification subscriptions ('watches')
 * for Confluence content.
 *
 * TODO: Consider splitting this up into separate read and write scopes
 */
public class ManageWatchersScope extends ConfluenceScope
{
    protected ManageWatchersScope()
    {
        super(asList(
                "watchPage",
                "watchSpace",
                "watchPageForUser",
                "isWatchingPage",
                "isWatchingSpace",
                "getWatchersForPage",
                "getWatchersForSpace",
                "isWatchingSpaceForType"
        ));
    }

    @Override
    public String getKey()
    {
        return "manage_watchers";
    }

    @Override
    public String getName()
    {
        return "Manage Watchers";
    }

    @Override
    public String getDescription()
    {
        return "View space, page and blog post watchers. Add new watchers.";
    }
}
