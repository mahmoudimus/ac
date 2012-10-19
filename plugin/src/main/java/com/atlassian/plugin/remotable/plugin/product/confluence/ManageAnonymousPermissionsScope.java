package com.atlassian.plugin.remotable.plugin.product.confluence;

import com.atlassian.plugin.remotable.api.service.confluence.ConfluencePermissions;

/**
 *
 */
public class ManageAnonymousPermissionsScope extends ConfluenceScope
{
    public ManageAnonymousPermissionsScope()
    {
        super(ConfluencePermissions.MANAGE_ANONYMOUS_PERMISSIONS);
    }
}
