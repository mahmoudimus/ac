package com.atlassian.labs.remoteapps.api.service.confluence.domain;

/**
 */
class AttachmentImpl implements MutableAttachment
{
    private long pageId;
    private String fileName;
    private String contentType;
    private String comment;

    @Override
    public long getPageId()
    {
        return pageId;
    }

    @Override
    public void setPageId(long pageId)
    {
        this.pageId = pageId;
    }

    @Override
    public String getFileName()
    {
        return fileName;
    }

    @Override
    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    @Override
    public String getContentType()
    {
        return contentType;
    }

    @Override
    public void setContentType(String contentType)
    {
        this.contentType = contentType;
    }

    @Override
    public String getComment()
    {
        return comment;
    }

    @Override
    public void setComment(String comment)
    {
        this.comment = comment;
    }
}
