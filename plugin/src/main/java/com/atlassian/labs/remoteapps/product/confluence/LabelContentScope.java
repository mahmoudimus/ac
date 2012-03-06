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

    @Override
    public String getKey()
    {
        return "label_content";
    }

    @Override
    public String getName()
    {
        return "Label Content";
    }

    @Override
    public String getDescription()
    {
        return "Add and remove labels from spaces, pages and blog posts";
    }
}
