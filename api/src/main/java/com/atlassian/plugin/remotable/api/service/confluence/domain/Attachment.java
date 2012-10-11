package com.atlassian.plugin.remotable.api.service.confluence.domain;

import com.atlassian.plugin.remotable.spi.util.RemoteName;

import java.net.URI;
import java.util.Date;

/**
 */
public interface Attachment
{
    long getId();

    long getPageId();

    String getTitle();

    String getFileName();

    long getFileSize();

    String getContentType();

    Date getCreated();

    @RemoteName("creator")
    String getCreatorName();

    URI getUrl();

    String getComment();
}
