package com.atlassian.plugin.connect.crowd.usermanagement.permissions;

public interface ConnectCrowdPermissions
{
    /**
     *
     * @param groupName
     * @return
     */
    public boolean setPermissionsForGroup(String groupName);
}
