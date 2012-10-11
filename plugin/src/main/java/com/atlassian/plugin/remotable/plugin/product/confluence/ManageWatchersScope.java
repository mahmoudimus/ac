package com.atlassian.plugin.remotable.plugin.product.confluence;

import com.atlassian.plugin.remotable.api.service.confluence.ConfluencePermission;

/**
 * API Scope for Confluence that grants Remotable Plugins the ability to vuew and modify notification subscriptions ('watches')
 * for Confluence content.
 *
 * TODO: Consider splitting this up into separate read and write scopes
 */
public class ManageWatchersScope extends ConfluenceScope
{
    protected ManageWatchersScope()
    {
        super(ConfluencePermission.MANAGE_WATCHERS);
    }
}
