package com.atlassian.plugin.remotable.api.service.confluence.domain;

/**
 */
public interface MutableContentPermission
{
    ContentPermissionType getType();

    String getUserName();

    String getGroupName();

    void setType(ContentPermissionType type);

    void setUserName(String userName);

    void setGroupName(String groupName);
}
