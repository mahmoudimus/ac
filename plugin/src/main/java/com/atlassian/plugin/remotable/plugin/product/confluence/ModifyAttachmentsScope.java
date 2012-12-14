package com.atlassian.plugin.remotable.plugin.product.confluence;

import com.atlassian.plugin.remotable.api.confluence.ConfluencePermissions;

/**
 *
 */
public class ModifyAttachmentsScope extends ConfluenceScope
{
    public ModifyAttachmentsScope()
    {
        super(ConfluencePermissions.MODIFY_ATTACHMENTS);
    }
}
