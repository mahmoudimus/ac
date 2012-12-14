package com.atlassian.plugin.remotable.plugin.product.confluence;

import com.atlassian.plugin.remotable.api.confluence.ConfluencePermissions;

/**
 * API Scope for Confluence that grants Remotable Plugins the ability to change the details of user accounts in Confluence.
 */
public class ModifyUsersScope extends ConfluenceScope
{
    protected ModifyUsersScope()
    {
        super(ConfluencePermissions.MODIFY_USERS);
    }
}
