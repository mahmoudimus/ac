package com.atlassian.plugin.connect.crowd.permissions;

public interface ConnectCrowdPermissions
{
    ConnectCrowdPermissionsImpl.GrantResult giveAdminPermission(String groupName);

    enum GrantResult
    {
        NO_REMOTE_GRANT_NEEDED,
        REMOTE_GRANT_FAILED,
        REMOTE_GRANT_SUCCEEDED
    }
}
