package com.atlassian.labs.remoteapps.plugin.product.confluence;

import com.atlassian.labs.remoteapps.api.service.confluence.ConfluencePermission;

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
