package com.atlassian.plugin.connect.crowd.permissions;

import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;

@JiraComponent
public class ConnectCrowdPermissionsClientImpl
        implements ConnectCrowdPermissionsClient
{
    @Override
    public boolean grantAdminPermission(String groupName)
    {
        return false;
    }
}
