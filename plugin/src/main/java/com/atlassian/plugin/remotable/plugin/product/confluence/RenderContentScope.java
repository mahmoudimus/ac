package com.atlassian.plugin.remotable.plugin.product.confluence;

import com.atlassian.plugin.remotable.api.confluence.ConfluencePermissions;

/**
 * API Scope for Confluence that grants Remotable Plugins the ability to access the render utility methods of the Confluence
 * Remote API.
 */
public class RenderContentScope extends ConfluenceScope
{
    public RenderContentScope()
    {
        super(ConfluencePermissions.RENDER_CONTENT);
    }
}
