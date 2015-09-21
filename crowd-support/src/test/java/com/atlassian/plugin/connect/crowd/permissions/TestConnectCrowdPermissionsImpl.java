package com.atlassian.plugin.connect.crowd.permissions;

import com.atlassian.plugin.connect.spi.product.FeatureManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static com.atlassian.plugin.connect.crowd.permissions.ConnectCrowdPermissions.GrantResult.NO_REMOTE_GRANT_NEEDED;
import static com.atlassian.plugin.connect.crowd.permissions.ConnectCrowdPermissions.GrantResult.REMOTE_GRANT_SUCCEEDED;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class TestConnectCrowdPermissionsImpl
{
    @Mock
    private ConnectCrowdPermissionsClient connectCrowdPermissionsClient;
    @Mock
    private FeatureManager featureManager;

    @Before
    public void beforeEach()
    {
        initMocks(this);

        when(featureManager.isOnDemand()).thenReturn(true);
        when(featureManager.isPermissionsManagedByUM()).thenReturn(true);
        when(connectCrowdPermissionsClient.grantAdminPermission(anyString()))
                .thenReturn(true);
    }

    @Test
    public void grantsAdminPermissionToSpecifiedGroup()
    {
        new ConnectCrowdPermissionsImpl(connectCrowdPermissionsClient, featureManager).giveAdminPermission("group-name");

        verify(connectCrowdPermissionsClient).grantAdminPermission("group-name");
    }

    @Test
    public void returnsSuccessWhenGrantSucceeds()
    {
        final ConnectCrowdPermissions connectCrowdPermissions =
                new ConnectCrowdPermissionsImpl(connectCrowdPermissionsClient, featureManager);
        assertThat(connectCrowdPermissions.giveAdminPermission("group-name"), is(REMOTE_GRANT_SUCCEEDED));
    }

    @Test
    public void reportsGrantIsNotRequiredWhenBTFOrUMFeatureIsOff()
    {
        final ConnectCrowdPermissions connectCrowdPermissions =
                new ConnectCrowdPermissionsImpl(connectCrowdPermissionsClient, featureManager);

        when(featureManager.isOnDemand()).thenReturn(false);
        when(featureManager.isPermissionsManagedByUM()).thenReturn(true);

        assertThat(connectCrowdPermissions.giveAdminPermission("group-name"), is(NO_REMOTE_GRANT_NEEDED));
        verifyZeroInteractions(connectCrowdPermissionsClient);

        when(featureManager.isOnDemand()).thenReturn(false);
        when(featureManager.isPermissionsManagedByUM()).thenReturn(false);

        assertThat(connectCrowdPermissions.giveAdminPermission("group-name"), is(NO_REMOTE_GRANT_NEEDED));
        verifyZeroInteractions(connectCrowdPermissionsClient);

        when(featureManager.isOnDemand()).thenReturn(true);
        when(featureManager.isPermissionsManagedByUM()).thenReturn(false);

        assertThat(connectCrowdPermissions.giveAdminPermission("group-name"), is(NO_REMOTE_GRANT_NEEDED));
        verifyZeroInteractions(connectCrowdPermissionsClient);
    }
}