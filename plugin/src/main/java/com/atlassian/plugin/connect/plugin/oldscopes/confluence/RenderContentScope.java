package com.atlassian.plugin.connect.plugin.oldscopes.confluence;

import com.atlassian.plugin.connect.api.confluence.ConfluencePermissions;

import static java.util.Arrays.asList;

/**
 * API Scope for Confluence that grants Remotable Plugins the ability to access the render utility methods of the Confluence
 * Remote API.
 */
public final class RenderContentScope extends ConfluenceScope
{
    public RenderContentScope()
    {
        super(ConfluencePermissions.RENDER_CONTENT,
                asList(
                        "renderContent",
                        "renderContent"
                )
                );
    }
}
