package com.atlassian.plugin.connect.crowd.permissions;

/**
 * A component for adding permissions remotely via the Horde unified user management interface
 */
public interface ConnectCrowdPermissions
{
    /**
     * Grant administer permissions to the group named, via Horde's unified user management
     *
     * @param groupName The name of the group to grant admin permission to
     * @return {@link com.atlassian.plugin.connect.crowd.permissions.ConnectCrowdPermissions.GrantResult#NO_REMOTE_GRANT_NEEDED} if permissions are not managed remotely,
     * {@link com.atlassian.plugin.connect.crowd.permissions.ConnectCrowdPermissions.GrantResult#REMOTE_GRANT_SUCCEEDED} if administer permissions were granted successfully, or
     * {@link com.atlassian.plugin.connect.crowd.permissions.ConnectCrowdPermissions.GrantResult#REMOTE_GRANT_FAILED} if granting of administer permissions failed
     */
    GrantResult giveAdminPermission(String groupName);

    enum GrantResult
    {
        NO_REMOTE_GRANT_NEEDED,
        REMOTE_GRANT_FAILED,
        REMOTE_GRANT_SUCCEEDED
    }
}
