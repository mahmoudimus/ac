package com.atlassian.labs.remoteapps.plugin.product.confluence;

import static java.util.Arrays.asList;

/**
 *
 */
public class ModifyAttachmentsScope extends ConfluenceScope
{
    public ModifyAttachmentsScope()
    {
        super(asList(
                "addAttachment",
                "removeAttachment",
                "moveAttachment"
        ));
    }

    @Override
    public String getKey()
    {
        return "modify_attachments";
    }

    @Override
    public String getName()
    {
        return "Modify Attachments";
    }

    @Override
    public String getDescription()
    {
        return "Add, remove, or move attachments for pages or blog posts";
    }
}
