package com.atlassian.plugin.connect.crowd.permissions;

public interface ConnectCrowdPermissions
{
    GrantResult giveAdminPermission(String groupName, String productId, String applicationId);

    enum GrantResult
    {
        NO_REMOTE_GRANT_NEEDED,
        REMOTE_GRANT_FAILED,
        REMOTE_GRANT_SUCCEEDED
    }
}
