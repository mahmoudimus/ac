package com.atlassian.plugin.connect.crowd.usermanagement.permissions;

import com.atlassian.plugin.connect.spi.host.HostProperties;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.usermanagement.client.api.ManagedPermissionsService;
import com.atlassian.usermanagement.client.api.UserManagementLockService;
import com.atlassian.usermanagement.client.api.exception.ManagedPermissionsException;
import com.atlassian.usermanagement.client.api.exception.UserManagementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@JiraComponent
public class ConnectCrowdPermissions
{
    private static final Logger log = LoggerFactory.getLogger(ConnectCrowdPermissions.class);

    private final HostProperties hostProperties;
    private final UserManagementLockService userManagementLockService;
    private final ManagedPermissionsService managedPermissionsService;

    @Autowired
    public ConnectCrowdPermissions(HostProperties hostProperties,
            UserManagementLockService userManagementLockService,
            ManagedPermissionsService managedPermissionsService)
    {
        this.hostProperties = hostProperties;
        this.userManagementLockService = userManagementLockService;
        this.managedPermissionsService = managedPermissionsService;
    }

    public boolean setPermissionsForGroup(String groupName)
    {

        try
        {
            userManagementLockService.unlockUserManagement();
        }
        catch (UserManagementException e)
        {
            log.warn("Could not unlock User Management. Some other Crowd process in control?", e); // Presuming locked. Does it error if locked already?
            return false;
        }

        grantAdminPermission(hostProperties.getKey(), groupName);

        try
        {
            managedPermissionsService.publishConfigToUserManagement();
        }
        catch (ManagedPermissionsException e)
        {
            log.warn("Could not publish admin changes to Crowd Remote");
            return false;
        }
        return true;
//https://dhaden-alt-test.atlassian.net/admin/rest/um/1/accessconfig/group?productId=product%3Ajira%3Ajira
    }


    private void grantAdminPermission(String productId, String groupName)
    {

    }
}
