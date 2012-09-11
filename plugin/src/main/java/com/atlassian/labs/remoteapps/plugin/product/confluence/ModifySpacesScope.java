package com.atlassian.labs.remoteapps.plugin.product.confluence;

import com.atlassian.labs.remoteapps.api.service.confluence.ConfluencePermission;

/**
 *
 */
public class ModifySpacesScope extends ConfluenceScope
{
    public ModifySpacesScope()
    {
        super(ConfluencePermission.MODIFY_SPACES);
    }
}
