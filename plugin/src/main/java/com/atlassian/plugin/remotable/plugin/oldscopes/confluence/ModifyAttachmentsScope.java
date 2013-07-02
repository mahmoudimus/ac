package com.atlassian.plugin.remotable.plugin.oldscopes.confluence;

import com.atlassian.plugin.remotable.api.confluence.ConfluencePermissions;

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
