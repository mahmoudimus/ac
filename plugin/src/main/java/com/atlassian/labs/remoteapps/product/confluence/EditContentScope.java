package com.atlassian.labs.remoteapps.product.confluence;

import static java.util.Arrays.asList;

/**
 * API Scope for Confluence that grants Remote Apps the ability to add, edit and remove pages, blogs and comments.
 */
public class EditContentScope extends ConfluenceScope
{
    protected EditContentScope()
    {
        super(asList(
                "storePage",
                "updatePage",
                "removePage",
                "movePage",
                "movePageToTopLevel",
                "storeBlogEntry",
                "addComment",
                "removeComment"
        ));
    }
}
