package com.atlassian.labs.remoteapps.api.service.confluence;

import com.atlassian.labs.remoteapps.api.Promise;
import com.atlassian.labs.remoteapps.api.service.confluence.domain.Attachment;
import com.atlassian.labs.remoteapps.api.service.confluence.domain.MutableAttachment;
import com.atlassian.labs.remoteapps.spi.util.RequirePermission;

import java.io.InputStream;

/**
 */
public interface ConfluenceAttachmentClient
{
    @RequirePermission(ConfluencePermission.MODIFY_ATTACHMENTS)
    Promise<Attachment> addAttachment(long contentId, MutableAttachment attachment, byte[] data);

    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<Attachment> getAttachment(long contentId, String fileName, String version);

    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<InputStream> getAttachmentData(long contentId, String fileName, String version);

    @RequirePermission(ConfluencePermission.MODIFY_ATTACHMENTS)
    Promise<Boolean> removeAttachment(long contentId, String fileName);

    @RequirePermission(ConfluencePermission.MODIFY_ATTACHMENTS)
    Promise<Boolean> moveAttachment(long contentId, String name, long newContentId, String newName);
}
