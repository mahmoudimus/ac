package com.atlassian.labs.remoteapps.api.service.confluence.domain;

/**
 *
 */
class PageImpl implements MutablePage
{
    private long id;
    private String spaceKey;
    private Long parentId;
    private String title;
    private int version;
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
    public Long getParentId()
    {
        return parentId;
    }

    @Override
    public void setParentId(Long parentId)
    {
        this.parentId = parentId;
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
    public int getVersion()
    {
        return version;
    }

    @Override
    public void setVersion(int version)
    {
        this.version = version;
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
