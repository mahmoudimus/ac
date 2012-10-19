package com.atlassian.plugin.remotable.plugin.product.confluence;

import com.atlassian.plugin.remotable.api.service.confluence.ConfluencePermissions;

/**
 *
 */
public class ModifySpacesScope extends ConfluenceScope
{
    public ModifySpacesScope()
    {
        super(ConfluencePermissions.MODIFY_SPACES);
    }
}
