package com.atlassian.labs.remoteapps.plugin.product.confluence;

import com.atlassian.labs.remoteapps.api.service.confluence.ConfluencePermission;

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
