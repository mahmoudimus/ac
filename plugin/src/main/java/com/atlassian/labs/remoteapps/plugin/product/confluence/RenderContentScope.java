package com.atlassian.labs.remoteapps.plugin.product.confluence;

import com.atlassian.labs.remoteapps.api.service.confluence.ConfluencePermission;

/**
 * API Scope for Confluence that grants Remote Apps the ability to access the render utility methods of the Confluence
 * Remote API.
 */
public class RenderContentScope extends ConfluenceScope
{
    public RenderContentScope()
    {
        super(ConfluencePermission.RENDER_CONTENT);
    }
}
