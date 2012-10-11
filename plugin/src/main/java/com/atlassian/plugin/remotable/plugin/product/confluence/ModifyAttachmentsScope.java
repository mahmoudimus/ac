package com.atlassian.plugin.remotable.plugin.product.confluence;

import com.atlassian.plugin.remotable.api.service.confluence.ConfluencePermission;

/**
 *
 */
public class ModifyAttachmentsScope extends ConfluenceScope
{
    public ModifyAttachmentsScope()
    {
        super(ConfluencePermission.MODIFY_ATTACHMENTS);
    }
}
