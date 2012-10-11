package com.atlassian.plugin.remotable.plugin.product.confluence;

import com.atlassian.plugin.remotable.api.service.confluence.ConfluencePermission;

/**
 *
 */
public class ManageAnonymousPermissionsScope extends ConfluenceScope
{
    public ManageAnonymousPermissionsScope()
    {
        super(ConfluencePermission.MANAGE_ANONYMOUS_PERMISSIONS);
    }
}
