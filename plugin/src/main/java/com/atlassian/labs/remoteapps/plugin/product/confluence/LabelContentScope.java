package com.atlassian.labs.remoteapps.plugin.product.confluence;

import com.atlassian.labs.remoteapps.api.service.confluence.ConfluencePermission;

/**
 * API Scope for Confluence that grants Remote Apps the ability to add and remove labels from Confluence content.
 */
public class LabelContentScope extends ConfluenceScope
{
    public LabelContentScope()
    {
        super(ConfluencePermission.LABEL_CONTENT);
    }
}
