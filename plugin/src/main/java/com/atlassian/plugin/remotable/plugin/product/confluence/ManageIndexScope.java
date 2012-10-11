package com.atlassian.plugin.remotable.plugin.product.confluence;

import com.atlassian.plugin.remotable.api.service.confluence.ConfluencePermission;

/**
 * API Scope for Confluence that grants Remotable Plugins the ability to add, edit and remove pages and blogs.
 */
public class ManageIndexScope extends ConfluenceScope
{
    protected ManageIndexScope()
    {
        super(ConfluencePermission.MANAGE_INDEX);
    }
}
