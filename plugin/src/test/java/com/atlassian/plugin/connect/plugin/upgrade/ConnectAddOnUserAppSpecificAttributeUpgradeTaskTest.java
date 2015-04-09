package com.atlassian.plugin.connect.plugin.upgrade;

import java.util.List;

import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.query.membership.MembershipQuery;
import com.atlassian.crowd.service.client.ClientProperties;
import com.atlassian.crowd.service.client.CrowdClient;
import com.atlassian.crowd.service.factory.CrowdClientFactory;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserGroupProvisioningService;
import com.atlassian.plugin.connect.plugin.util.FeatureManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import static com.atlassian.crowd.search.query.entity.EntityQuery.ALL_RESULTS;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ConnectAddOnUserAppSpecificAttributeUpgradeTaskTest
{
    private ConnectAddOnUserAppSpecificAttributeUpgradeTask upgradeTask;

    @Mock
    private FeatureManager featureManager;
    @Mock
    private CrowdClientFactory crowdClientFactory;
    @Mock
    private CrowdClient crowdClient;

    @Mock
    private ApplicationService applicationService;
    @Mock
    Application application;

    @Mock
    ConnectAddOnUserGroupProvisioningService addOnUserGroupProvisioningService;

    @Before
    public void setup() throws Exception
    {
        initMocks(this);
        when(addOnUserGroupProvisioningService.getCrowdApplication()).thenReturn(application);
        when(featureManager.isOnDemand()).thenReturn(true);
        when(crowdClientFactory.newInstance(any(ClientProperties.class))).thenReturn(crowdClient);
        upgradeTask = new ConnectAddOnUserAppSpecificAttributeUpgradeTask(applicationService, addOnUserGroupProvisioningService, crowdClientFactory, featureManager);
    }

    @Test
    public void onlyUpdatesUsersInAddonGroup() throws Exception
    {
        upgradeTask.doUpgrade();

        ArgumentCaptor<MembershipQuery> userQueryCaptor = ArgumentCaptor.forClass(MembershipQuery.class);
        verify(applicationService).searchDirectGroupRelationships(any(Application.class), userQueryCaptor.capture());

        @SuppressWarnings ("unchecked")
        MembershipQuery<User> userQuery = userQueryCaptor.getValue();

        assertThat(userQuery.isFindChildren(), is(true));
        assertThat(userQuery.getEntityNameToMatch(), is("atlassian-addons"));
        assertThat(userQuery.getEntityToMatch(), is(EntityDescriptor.group()));
        assertThat(userQuery.getMaxResults(), is(ALL_RESULTS));
    }

    @Test
    public void updatesUsersForCurrentApplication() throws Exception
    {
        upgradeTask.doUpgrade();

        verify(applicationService).searchDirectGroupRelationships(eq(application), any(MembershipQuery.class));
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void updatesAttributeForRelevantApplication() throws Exception
    {
        setupMockAddonUsers();
        upgradeTask.doUpgrade();
        verify(applicationService).storeUserAttributes(eq(application), anyString(), anyMap());
    }

    private void setupMockAddonUsers(String... names)
    {
        String userNames[] = (names.length > 0) ? names : new String[] { "addon_fugitive" };
        List<User> users = newArrayList();

        for (String name : userNames)
        {
            User mockUser = mock(User.class);
            when(mockUser.getName()).thenReturn(name);
            when(mockUser.getEmailAddress()).thenReturn("noreply@mailer.atlassian.com");
            users.add(mockUser);
        }

        when(applicationService.searchDirectGroupRelationships(any(Application.class), any(MembershipQuery.class)))
                .thenReturn(users);
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void updatesAttributeForRelevantUser() throws Exception
    {
        setupMockAddonUsers("addon_care", "addon_wanna.miss.a.tha-ang");
        upgradeTask.doUpgrade();

        verify(applicationService).storeUserAttributes(any(Application.class), eq("addon_care"), anyMap());
        verify(applicationService).storeUserAttributes(any(Application.class), eq("addon_wanna.miss.a.tha-ang"), anyMap());
        verify(crowdClient).storeUserAttributes(eq("addon_care"), anyMap());
        verify(crowdClient).storeUserAttributes(eq("addon_wanna.miss.a.tha-ang"), anyMap());
    }

    @Test (expected = Exception.class)
    public void validatesAddonUserName() throws Exception
    {
        setupMockAddonUsers("you_give_addon_a.bad.name");

        upgradeTask.doUpgrade();
    }

    @Test (expected = Exception.class)
    public void validatesAddonEmailAddress() throws Exception
    {
        User mockUser = mock(User.class);
        when(mockUser.getName()).thenReturn("addon_valid-name");
        when(mockUser.getEmailAddress()).thenReturn("addon_invalid-address@example.com");

        when(applicationService.searchDirectGroupRelationships(any(Application.class), any(MembershipQuery.class)))
                .thenReturn(singletonList(mockUser));

        upgradeTask.doUpgrade();
    }

    @Test
    public void skipsRemoteUpdateForBTF() throws Exception
    {
        setupMockAddonUsers("addon_care");
        when(featureManager.isOnDemand()).thenReturn(false);

        upgradeTask.doUpgrade();
        verify(applicationService).storeUserAttributes(any(Application.class), anyString(), anyMap());
        verify(crowdClient, never()).storeUserAttributes(anyString(), anyMap());
    }

}
