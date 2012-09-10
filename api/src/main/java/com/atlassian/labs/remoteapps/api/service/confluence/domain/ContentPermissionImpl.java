package com.atlassian.labs.remoteapps.api.service.confluence.domain;

/**
 */
class ContentPermissionImpl implements MutableContentPermission
{
    private ContentPermissionType type;
    private String userName;
    private String groupName;

    @Override
    public ContentPermissionType getType()
    {
        return type;
    }

    @Override
    public void setType(ContentPermissionType type)
    {
        this.type = type;
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
    public String getGroupName()
    {
        return groupName;
    }

    @Override
    public void setGroupName(String groupName)
    {
        this.groupName = groupName;
    }
}
