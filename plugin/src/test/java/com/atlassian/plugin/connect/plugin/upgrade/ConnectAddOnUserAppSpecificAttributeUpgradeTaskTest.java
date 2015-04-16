package com.atlassian.plugin.connect.plugin.upgrade;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.service.client.CrowdClient;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserGroupProvisioningService;
import com.atlassian.plugin.connect.plugin.usermanagement.CrowdClientFacade;
import com.atlassian.plugin.connect.plugin.util.FeatureManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.singletonList;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ConnectAddOnUserAppSpecificAttributeUpgradeTaskTest
{
    public static final Set<String> EXPECTED_ATTRIBUTE_VALUE = new HashSet<>(singletonList("true"));
    private ConnectAddOnUserAppSpecificAttributeUpgradeTask upgradeTask;

    @Mock
    private FeatureManager featureManager;
    @Mock
    private CrowdClientFacade crowdClientFacade;
    @Mock
    private CrowdClient crowdClient;
    @Mock
    private ConnectAddOnUsers connectAddOnUsers;

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
        when(crowdClientFacade.getCrowdClient()).thenReturn(crowdClient);
        when(crowdClientFacade.getClientApplicationName()).thenReturn("the-app-name");
        upgradeTask = new ConnectAddOnUserAppSpecificAttributeUpgradeTask(applicationService, connectAddOnUsers, addOnUserGroupProvisioningService, crowdClientFacade, featureManager);
    }

    @Test
    public void onlyUpdatesHostProductAddonUsers() throws Exception
    {
        setupMockAddonUsers();

        upgradeTask.doUpgrade();

        verify(connectAddOnUsers).getAddonUsersToUpgradeForHostProduct();
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void updatesAttributeForRelevantApplication() throws Exception
    {
        setupMockAddonUsers();
        upgradeTask.doUpgrade();
        verify(applicationService).storeUserAttributes(eq(application), anyString(), anyMap());
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

        when(connectAddOnUsers.getAddonUsersToUpgradeForHostProduct()).thenReturn(singletonList(mockUser));

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

    @Test
    public void namesAttributeBasedOnHostApplication() throws Exception
    {
        setupMockAddonUsers();

        upgradeTask.doUpgrade();

        verify(applicationService).storeUserAttributes(any(Application.class), anyString(), attributeCalled("synch.the-app-name.atlassian-connect-user"));
        verify(crowdClient).storeUserAttributes(anyString(), attributeCalled("synch.the-app-name.atlassian-connect-user"));
    }

    @Test
    public void deletesOldAttribute() throws Exception
    {
        setupMockAddonUsers("addon_mind");

        upgradeTask.doUpgrade();

        verify(applicationService).removeUserAttributes(application, "addon_mind", "synch.crowd-embedded.atlassian-connect-user");
        verify(crowdClient).removeUserAttributes("addon_mind", "synch.crowd-embedded.atlassian-connect-user");
    }

    private void setupMockAddonUsers(String... names)
            throws ApplicationNotFoundException
    {
        String userNames[] = (names.length > 0) ? names : new String[] { "addon_default" };
        List<User> users = newArrayList();

        for (String name : userNames)
        {
            User mockUser = mock(User.class);
            when(mockUser.getName()).thenReturn(name);
            when(mockUser.getEmailAddress()).thenReturn("noreply@mailer.atlassian.com");
            users.add(mockUser);
        }

        when(connectAddOnUsers.getAddonUsersToUpgradeForHostProduct()).thenReturn(users);
    }

    @SuppressWarnings ("unchecked")
    private static Map<String, Set<String>> attributeCalled(String attributeName)
    {
        return (Map<String, Set<String>>) argThat(hasEntry(attributeName, EXPECTED_ATTRIBUTE_VALUE));
    }
}
