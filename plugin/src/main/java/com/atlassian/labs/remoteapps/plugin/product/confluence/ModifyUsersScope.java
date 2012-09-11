package com.atlassian.labs.remoteapps.plugin.product.confluence;

import com.atlassian.labs.remoteapps.api.service.confluence.ConfluencePermission;

/**
 * API Scope for Confluence that grants Remote Apps the ability to change the details of user accounts in Confluence.
 */
public class ModifyUsersScope extends ConfluenceScope
{
    protected ModifyUsersScope()
    {
        super(ConfluencePermission.MODIFY_USERS);
    }
}
