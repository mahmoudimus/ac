package com.atlassian.labs.remoteapps.plugin.product.confluence;

import com.atlassian.labs.remoteapps.api.service.confluence.ConfluencePermission;

/**
 * API Scope for Confluence that grants Remote Apps the ability to add, edit and remove pages and blogs.
 */
public class ModifyContentScope extends ConfluenceScope
{
    protected ModifyContentScope()
    {
        super(ConfluencePermission.MODIFY_CONTENT);
    }
}
