package com.atlassian.labs.remoteapps.product.confluence;

import static java.util.Arrays.asList;

/**
 *
 */
public class ModifySpacesScope extends ConfluenceScope
{
    public ModifySpacesScope()
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

    @Override
    public String getKey()
    {
        return "modify_spaces";
    }

    @Override
    public String getName()
    {
        return "Modify Spaces";
    }

    @Override
    public String getDescription()
    {
        return "Add, edit and remove spaces";
    }
}
