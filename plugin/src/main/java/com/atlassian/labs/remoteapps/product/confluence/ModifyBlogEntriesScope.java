package com.atlassian.labs.remoteapps.product.confluence;

import static java.util.Arrays.asList;

/**
 *
 */
public class ModifyBlogEntriesScope extends ConfluenceScope
{
    public ModifyBlogEntriesScope()
    {
        super(asList(
                "storeBlogEntry"
        ));
    }
}
