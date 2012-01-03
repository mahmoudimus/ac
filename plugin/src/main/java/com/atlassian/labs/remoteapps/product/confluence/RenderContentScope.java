package com.atlassian.labs.remoteapps.product.confluence;

import static java.util.Arrays.asList;

/**
 * API Scope for Confluence that grants Remote Apps the ability to access the render utility methods of the Confluence
 * Remote API.
 * TODO: Consider folding this scope into the {@link ReadContentScope} API Scope.
 */
public class RenderContentScope extends ConfluenceScope
{
    public RenderContentScope()
    {
        super(asList(
                "convertWikiToStorageFormat",
                "renderContent"
        ));
    }
}
