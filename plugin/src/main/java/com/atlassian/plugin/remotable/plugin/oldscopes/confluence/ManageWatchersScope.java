package com.atlassian.plugin.remotable.plugin.oldscopes.confluence;

import com.atlassian.plugin.remotable.api.confluence.ConfluencePermissions;

import static java.util.Arrays.asList;

/**
 * API Scope for Confluence that grants Remotable Plugins the ability to vuew and modify notification subscriptions ('watches')
 * for Confluence content.
 *
 * TODO: Consider splitting this up into separate read and write scopes
 */
public final class ManageWatchersScope extends ConfluenceScope
{
    public ManageWatchersScope()
    {
        super(ConfluencePermissions.MANAGE_WATCHERS,
                asList(
                        "watchPage",
                        "watchPageForUser",
                        "watchSpace",
                        "removePageWatch",
                        "removeSpaceWatch",
                        "removePageWatchForUser"
                )
                );
    }
}
