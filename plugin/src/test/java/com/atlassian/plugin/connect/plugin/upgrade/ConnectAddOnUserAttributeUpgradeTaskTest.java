package com.atlassian.plugin.connect.plugin.upgrade;

import java.util.Map;
import java.util.Set;

import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.crowd.search.query.membership.MembershipQuery;
import com.atlassian.crowd.service.client.CrowdClient;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserGroupProvisioningService;
import com.atlassian.plugin.connect.plugin.usermanagement.CrowdClientFacade;
import com.atlassian.plugin.connect.plugin.util.FeatureManager;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserUtil.Constants.ADDON_USERNAME_PREFIX;
import static com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserUtil.Constants.ADDON_USER_EMAIL_ADDRESS;
import static com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserUtil.buildAttributeConnectAddOnAttributeName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConnectAddOnUserAttributeUpgradeTaskTest
{
    private static final String APPLICATION_NAME = "crowd-embedded";
    private ConnectAddOnUserAttributeUpgradeTask upgradeTask;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Mock
    private ApplicationService applicationService;
    @Mock
    private ApplicationManager applicationManager;
    @Mock
    private ConnectAddOnUserGroupProvisioningService connectAddOnUserGroupProvisioningService;
    @Mock
    private Application application;
    @Mock
    private CrowdClient crowdClient;
    @Mock
    private CrowdClientFacade crowdClientFacade;
    @Mock
    private FeatureManager featureManager;
    @Captor
    private ArgumentCaptor<Map<String, Set<String>>> localUserAttributeCaptor;
    @Captor
    private ArgumentCaptor<Map<String, Set<String>>> remoteUserAttributeCaptor;

    private static final String VALID_CONNECT_ADD_ON_USERNAME = ADDON_USERNAME_PREFIX + "correct_user";

    @Before
    public void setup()
    {
        upgradeTask = new ConnectAddOnUserAttributeUpgradeTask(applicationService, applicationManager, connectAddOnUserGroupProvisioningService, featureManager, crowdClientFacade);
    }

    @Test
    public void testDoUpgrade() throws Exception
    {
        UserTemplate userTemplate = new UserTemplate(VALID_CONNECT_ADD_ON_USERNAME);
        userTemplate.setEmailAddress(ADDON_USER_EMAIL_ADDRESS);

        when(application.getName()).thenReturn(APPLICATION_NAME);
        when(connectAddOnUserGroupProvisioningService.getCrowdApplicationName()).thenReturn(APPLICATION_NAME);
        when(applicationManager.findByName(APPLICATION_NAME)).thenReturn(application);
        when(applicationService.searchDirectGroupRelationships(eq(application), any(MembershipQuery.class))).thenReturn(ImmutableList.of(userTemplate));
        when(crowdClientFacade.getCrowdClient()).thenReturn(crowdClient);
        when(featureManager.isOnDemand()).thenReturn(true);

        upgradeTask.doUpgrade();

        verify(applicationService).storeUserAttributes(eq(application), eq(VALID_CONNECT_ADD_ON_USERNAME), localUserAttributeCaptor.capture());
        verify(crowdClient).storeUserAttributes(eq(VALID_CONNECT_ADD_ON_USERNAME), remoteUserAttributeCaptor.capture());

        assertThat(localUserAttributeCaptor.getValue().keySet(), containsInAnyOrder(buildAttributeConnectAddOnAttributeName(APPLICATION_NAME)));
        assertThat(remoteUserAttributeCaptor.getValue().keySet(), containsInAnyOrder(buildAttributeConnectAddOnAttributeName(APPLICATION_NAME)));
    }

    @Test
    public void testDoUpgradeWhenNotRunningInOnDemand() throws Exception
    {
        UserTemplate userTemplate = new UserTemplate(VALID_CONNECT_ADD_ON_USERNAME);
        userTemplate.setEmailAddress(ADDON_USER_EMAIL_ADDRESS);

        when(application.getName()).thenReturn(APPLICATION_NAME);
        when(connectAddOnUserGroupProvisioningService.getCrowdApplicationName()).thenReturn(APPLICATION_NAME);
        when(applicationManager.findByName(APPLICATION_NAME)).thenReturn(application);
        when(applicationService.searchDirectGroupRelationships(eq(application), any(MembershipQuery.class))).thenReturn(ImmutableList.of(userTemplate));
        when(featureManager.isOnDemand()).thenReturn(false);
        when(crowdClientFacade.getCrowdClient()).thenReturn(crowdClient);

        upgradeTask.doUpgrade();

        verify(crowdClient, never()).storeUserAttributes(anyString(), anyMap());
        verify(applicationService).storeUserAttributes(eq(application), eq(VALID_CONNECT_ADD_ON_USERNAME), localUserAttributeCaptor.capture());

        assertThat(localUserAttributeCaptor.getValue().keySet(), containsInAnyOrder(buildAttributeConnectAddOnAttributeName(APPLICATION_NAME)));
    }

    @Test
    public void testDoUpgradeFailsWhenAddonUserInformationIsInvalid() throws Exception
    {
        expectedException.expect(Exception.class);
        expectedException.expectMessage("Failed to complete Upgrade Task.");

        when(application.getName()).thenReturn(APPLICATION_NAME);
        when(connectAddOnUserGroupProvisioningService.getCrowdApplicationName()).thenReturn(APPLICATION_NAME);
        when(applicationManager.findByName(APPLICATION_NAME)).thenReturn(application);

        when(applicationService.searchDirectGroupRelationships(eq(application), any((MembershipQuery.class)))).thenReturn(ImmutableList.of(new UserTemplate("bad-connect-add-on-name")));

        upgradeTask.doUpgrade();

        verify(applicationService, never()).storeUserAttributes(any(Application.class), anyString(), anyMap());
        verify(crowdClient, never()).storeUserAttributes(anyString(), anyMap());
    }
}