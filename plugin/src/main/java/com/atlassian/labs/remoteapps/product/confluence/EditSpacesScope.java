package com.atlassian.labs.remoteapps.product.confluence;

import static java.util.Arrays.asList;

/**
 *
 */
public class EditSpacesScope extends ConfluenceScope
{
    public EditSpacesScope()
    {
        super(asList(
                "getSpaces",
                "removeSpace",
                "addPersonalSpace",
                "convertToPersonalSpace",
                "storeSpace"
                // TODO: Consider whether or not the importSpace method should be permitted. I have omitted it for now, since it is stupidly memory intensive.
        ));
    }
}
