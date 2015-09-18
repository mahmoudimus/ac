package com.atlassian.plugin.connect.crowd.permissions;

import com.atlassian.plugin.connect.spi.product.FeatureManager;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.usermanagement.client.api.ManagedPermissionsService;
import com.atlassian.usermanagement.client.api.UserManagementLockService;
import com.atlassian.usermanagement.client.api.exception.UserManagementException;

import org.springframework.beans.factory.annotation.Autowired;

import static com.atlassian.plugin.connect.crowd.permissions.ConnectCrowdPermissions.GrantResult.NO_REMOTE_GRANT_NEEDED;
import static com.atlassian.plugin.connect.crowd.permissions.ConnectCrowdPermissions.GrantResult.REMOTE_GRANT_FAILED;
import static com.atlassian.plugin.connect.crowd.permissions.ConnectCrowdPermissions.GrantResult.REMOTE_GRANT_SUCCEEDED;

@JiraComponent
public class ConnectCrowdPermissionsImpl implements ConnectCrowdPermissions
{
    private final UserManagementLockService userManagementLockService;
    private final ManagedPermissionsService managedPermissionsService;
    private final FeatureManager featureManager;

    @Autowired
    public ConnectCrowdPermissionsImpl(
            UserManagementLockService userManagementLockService,
            ManagedPermissionsService managedPermissionsService,
            FeatureManager featureManager)
    {
        this.userManagementLockService = userManagementLockService;
        this.managedPermissionsService = managedPermissionsService;
        this.featureManager = featureManager;
    }


    @Override
    public GrantResult giveAdminPermission(String groupName)
    {
        GrantResult result = REMOTE_GRANT_FAILED;
        if (!featureManager.isOnDemand())
        {
            return NO_REMOTE_GRANT_NEEDED;
        }
        else
        {
            try
            {
                userManagementLockService.unlockUserManagement();
                grantAdminPermission(groupName);
                managedPermissionsService.publishConfigToUserManagement();
            }
            catch (UserManagementException ignored)
            {
                // TODO Logging
                return REMOTE_GRANT_FAILED;
            }
        }
        return REMOTE_GRANT_SUCCEEDED;
    }

    private boolean grantAdminPermission(String groupName)
    {
        return false;
    }
}
