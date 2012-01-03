package com.atlassian.labs.remoteapps.product.confluence;

import static java.util.Arrays.asList;

/**
 * API Scope for Confluence that grants Remote Apps the ability to add and remove labels from Confluence content.
 */
public class LabelContentScope extends ConfluenceScope
{
    public LabelContentScope()
    {
        super(asList(
                "addLabelByName",
                "addLabelById",
                "addLabelByObject",
                "addLabelByNameToSpace",
                "removeLabelByName",
                "removeLabelById",
                "removeLabelByNameFromSpace"
        ));
    }
}
