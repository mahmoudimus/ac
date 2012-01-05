package com.atlassian.labs.remoteapps.product.confluence;

import static java.util.Arrays.asList;

/**
 * API Scope for Confluence that grants Remote Apps the ability to add and remove comments.
 */
public class ModifyCommentsScope extends ConfluenceScope
{
    protected ModifyCommentsScope()
    {
        super(asList(
                "addComment",
                "removeComment"));
    }
}
