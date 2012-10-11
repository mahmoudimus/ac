package com.atlassian.plugin.remotable.api.service.confluence.domain;

/**
 */
public interface MutableAttachment
{
    void setPageId(long pageId);

    void setFileName(String fileName);

    void setContentType(String contentType);

    void setComment(String comment);

    long getPageId();

    String getFileName();

    String getContentType();

    String getComment();
}
