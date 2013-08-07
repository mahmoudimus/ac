package com.atlassian.plugin.connect.plugin.oldscopes.confluence;

import com.atlassian.plugin.connect.api.confluence.ConfluencePermissions;

import static java.util.Arrays.asList;

/**
 * API Scope for Confluence that grants Remotable Plugins the ability to add and remove labels from Confluence content.
 */
public final class LabelContentScope extends ConfluenceScope
{
    public LabelContentScope()
    {
        super(ConfluencePermissions.LABEL_CONTENT
                ,asList(
                        "addLabelByName",
                        "addLabelById",
                        "addLabelByObject",
                        "addLabelByNameToSpace",
                        "removeLabelByName",
                        "removeLabelById",
                        "removeLabelByObject",
                        "removeLabelByNameFromSpace"
                ));
    }
}
