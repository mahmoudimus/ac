package com.atlassian.plugin.connect.plugin.oldscopes.confluence;

import com.atlassian.plugin.connect.api.confluence.ConfluencePermissions;

import static java.util.Arrays.asList;

public final class ModifyAttachmentsScope extends ConfluenceScope
{
    public ModifyAttachmentsScope()
    {
        super(ConfluencePermissions.MODIFY_ATTACHMENTS,
                asList(
                        "addAttachment",
                        "removeAttachment",
                        "moveAttachment"
                )
                );
    }
}
