package com.atlassian.labs.remoteapps.api.service.confluence.domain;

/**
 *
 */
class SpaceImpl implements MutableSpace
{
    private String key;
    private String name;
    private Long homePageId;
    private String description;

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public String getKey()
    {
        return key;
    }

    @Override
    public void setKey(String key)
    {
        this.key = key;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public void setDescription(String description)
    {
        this.description = description;
    }

    @Override
    public Long getHomePageId()
    {
        return homePageId;
    }

    @Override
    public void setHomePageId(Long homePageId)
    {
        this.homePageId = homePageId;
    }
}
