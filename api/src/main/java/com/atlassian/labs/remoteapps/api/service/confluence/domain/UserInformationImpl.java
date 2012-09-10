package com.atlassian.labs.remoteapps.api.service.confluence.domain;

/**
 */
class UserInformationImpl implements MutableUserInformation
{
    private long id;
    private String userName;
    private String description;

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
    public String getUserName()
    {
        return userName;
    }

    @Override
    public void setUserName(String userName)
    {
        this.userName = userName;
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
}
