package com.atlassian.plugin.remotable.api.service.confluence;

import com.atlassian.plugin.remotable.api.service.confluence.domain.Attachment;
import com.atlassian.plugin.remotable.api.service.confluence.domain.MutableAttachment;
import com.atlassian.plugin.remotable.spi.util.RequirePermission;
import com.atlassian.util.concurrent.Promise;

import java.io.InputStream;

/**
 */
public interface ConfluenceAttachmentClient
{
    @RequirePermission(ConfluencePermissions.MODIFY_ATTACHMENTS)
    Promise<Attachment> addAttachment(long contentId, MutableAttachment attachment, byte[] data);

    @RequirePermission(ConfluencePermissions.READ_CONTENT)
    Promise<Attachment> getAttachment(long contentId, String fileName, String version);

    @RequirePermission(ConfluencePermissions.READ_CONTENT)
    Promise<InputStream> getAttachmentData(long contentId, String fileName, String version);

    @RequirePermission(ConfluencePermissions.MODIFY_ATTACHMENTS)
    Promise<Boolean> removeAttachment(long contentId, String fileName);

    @RequirePermission(ConfluencePermissions.MODIFY_ATTACHMENTS)
    Promise<Boolean> moveAttachment(long contentId, String name, long newContentId, String newName);
}
