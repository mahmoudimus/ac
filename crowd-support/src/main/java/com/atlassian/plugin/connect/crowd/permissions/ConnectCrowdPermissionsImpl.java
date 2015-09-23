package com.atlassian.plugin.connect.crowd.permissions;

import com.atlassian.plugin.connect.spi.product.FeatureManager;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;

import org.springframework.beans.factory.annotation.Autowired;

import static com.atlassian.plugin.connect.crowd.permissions.ConnectCrowdPermissions.GrantResult.NO_REMOTE_GRANT_NEEDED;
import static com.atlassian.plugin.connect.crowd.permissions.ConnectCrowdPermissions.GrantResult.REMOTE_GRANT_FAILED;
import static com.atlassian.plugin.connect.crowd.permissions.ConnectCrowdPermissions.GrantResult.REMOTE_GRANT_SUCCEEDED;

@JiraComponent
public class ConnectCrowdPermissionsImpl implements ConnectCrowdPermissions
{
    private final ConnectCrowdPermissionsClient connectCrowdPermissionsClient;
    private final FeatureManager featureManager;

    @Autowired
    public ConnectCrowdPermissionsImpl(
            ConnectCrowdPermissionsClient connectCrowdPermissionsClient,
            FeatureManager featureManager)
    {
        this.connectCrowdPermissionsClient = connectCrowdPermissionsClient;
        this.featureManager = featureManager;
    }

    @Override
    public GrantResult giveAdminPermission(String groupName, String productId, String applicationId)
    {
        if (featureManager.isOnDemand() && featureManager.isPermissionsManagedByUM())
        {
            return connectCrowdPermissionsClient.grantAdminPermission(groupName, productId, applicationId) ? REMOTE_GRANT_SUCCEEDED : REMOTE_GRANT_FAILED;
        }
        else
        {
            return NO_REMOTE_GRANT_NEEDED;
        }
    }

}
