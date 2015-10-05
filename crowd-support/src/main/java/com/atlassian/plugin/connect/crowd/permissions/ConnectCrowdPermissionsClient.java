package com.atlassian.plugin.connect.crowd.permissions;

public interface ConnectCrowdPermissionsClient
{
    boolean grantAdminPermission(String groupName, String productId, String applicationId);
}
