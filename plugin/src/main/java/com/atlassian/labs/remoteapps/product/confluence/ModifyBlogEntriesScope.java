package com.atlassian.labs.remoteapps.product.confluence;

import static java.util.Arrays.asList;

/**
 * @deprecated Use {@link ModifyContentScope} instead.
 */
@Deprecated
public class ModifyBlogEntriesScope extends ConfluenceScope
{
    public ModifyBlogEntriesScope()
    {
        super(asList(
                "storeBlogEntry"
        ));
    }
}
