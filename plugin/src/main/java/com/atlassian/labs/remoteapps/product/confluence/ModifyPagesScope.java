package com.atlassian.labs.remoteapps.product.confluence;

import static java.util.Arrays.asList;

/**
 *
 */
public class ModifyPagesScope extends ConfluenceScope
{
    public ModifyPagesScope()
    {
        super(asList(
                "storePage",
                "updatePage",
                "movePage",
                "movePageToTopLevel",
                "removePage"
        ));
    }
}
