package com.atlassian.plugin.remotable.plugin.product.confluence;

import com.atlassian.plugin.remotable.api.service.confluence.ConfluencePermission;

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
