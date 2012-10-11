package com.atlassian.plugin.remotable.api.service.confluence.domain;

/**
 */
class CommentImpl implements MutableComment
{
    private long id;
    private long pageId;
    private String content;

    @Override
    public long getId()
    {
        return id;
    }

    @Override
    public void setId(long id)
    {
        this.id = id;
    }

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
    public String getContent()
    {
        return content;
    }

    @Override
    public void setContent(String content)
    {
        this.content = content;
    }
}
