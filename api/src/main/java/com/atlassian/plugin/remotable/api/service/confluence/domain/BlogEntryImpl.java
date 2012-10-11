package com.atlassian.plugin.remotable.api.service.confluence.domain;

/**
 */
class BlogEntryImpl implements MutableBlogEntry
{
    private long id;
    private String spaceKey;
    private String title;
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
    public String getSpaceKey()
    {
        return spaceKey;
    }

    @Override
    public void setSpaceKey(String spaceKey)
    {
        this.spaceKey = spaceKey;
    }

    @Override
    public String getTitle()
    {
        return title;
    }

    @Override
    public void setTitle(String title)
    {
        this.title = title;
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
