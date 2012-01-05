package com.atlassian.labs.remoteapps.product.confluence;

import static java.util.Arrays.asList;

/**
 * API Scope for Confluence that grants Remote Apps the ability to add, edit and remove pages and blogs.
 */
public class ModifyContentScope extends ConfluenceScope
{
    protected ModifyContentScope()
    {
        super(asList(
                "storePage",
                "updatePage",
                "removePage",
                "movePage",
                "movePageToTopLevel",
                "storeBlogEntry"
        ));
    }
}
