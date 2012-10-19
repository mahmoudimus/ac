package com.atlassian.plugin.remotable.plugin.product.confluence;

import com.atlassian.plugin.remotable.api.service.confluence.ConfluencePermissions;

/**
 * API Scope for Confluence that grants Remotable Plugins the ability to add and remove labels from Confluence content.
 */
public class LabelContentScope extends ConfluenceScope
{
    public LabelContentScope()
    {
        super(ConfluencePermissions.LABEL_CONTENT);
    }
}
