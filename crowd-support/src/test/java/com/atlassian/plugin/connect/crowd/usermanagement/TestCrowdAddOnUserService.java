package com.atlassian.plugin.connect.crowd.usermanagement;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserGroupProvisioningService;
import com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserProvisioningService;
import com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserUtil;
import com.atlassian.plugin.connect.spi.host.HostProperties;

import com.google.common.collect.Sets;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserUtil.Constants.ADDON_USER_GROUP_KEY;
import static com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserUtil.buildConnectAddOnUserAttribute;
import static com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserUtil.usernameForAddon;
import static com.atlassian.plugin.connect.crowd.usermanagement.UserCreationResult.UserNewness.NEWLY_CREATED;
import static com.atlassian.plugin.connect.crowd.usermanagement.UserCreationResult.UserNewness.PRE_EXISTING;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class TestCrowdAddOnUserService
{
    public static final String PRODUCT_KEY = "the-product";
    public static final String ADDON_KEY = "the-addon-key";
    public static final String ADDON_USERNAME = usernameForAddon(ADDON_KEY);
    public static final String ADDON_NAME = "The Addon";
    private CrowdAddOnUserService crowdAddOnUserService;

    @Mock
    private ConnectAddOnUserProvisioningService connectAddOnUserProvisioningService;
    @Mock
    private ConnectAddOnUserGroupProvisioningService connectAddOnUserGroupProvisioningService;
    @Mock
    private ConnectCrowdService connectCrowdService;
    @Mock
    private HostProperties hostProperties;
    @Mock
    private User user;

    @Before
    public void beforeEach()
    {
        initMocks(this);
        when(user.getName()).thenReturn(ADDON_USERNAME);
        when(connectCrowdService.createOrEnableUser(anyString(), anyString(), anyString(),
                any(PasswordCredential.class), anyMap())).thenReturn(new UserCreationResult(user, NEWLY_CREATED));
        crowdAddOnUserService = new CrowdAddOnUserService(
                connectAddOnUserProvisioningService,
                connectAddOnUserGroupProvisioningService,
                connectCrowdService,
                hostProperties);
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void getOrCreateUserNameCreatesAddonUser() throws Exception
    {
        when(hostProperties.getKey()).thenReturn(PRODUCT_KEY);
        crowdAddOnUserService.getOrCreateUserName(ADDON_KEY, ADDON_NAME);

        verify(connectCrowdService).createOrEnableUser(ConnectAddOnUserUtil.usernameForAddon(ADDON_KEY),
                ADDON_NAME, ConnectAddOnUserUtil.Constants.ADDON_USER_EMAIL_ADDRESS,
                PasswordCredential.NONE, buildConnectAddOnUserAttribute(PRODUCT_KEY));
    }

    @Test
    public void getOrCreateUserNameCreatesAddonGroup() throws Exception
    {
        crowdAddOnUserService.getOrCreateUserName(ADDON_KEY, ADDON_NAME);
        verify(connectAddOnUserGroupProvisioningService).ensureGroupExists(ADDON_USER_GROUP_KEY);
    }

    @Test
    public void getOrCreateUserNameAddsAddonUserToGroupsWhenUserIsNew() throws Exception
    {
        when(connectAddOnUserProvisioningService.getDefaultProductGroupsAlwaysExpected()).thenReturn(Sets.newHashSet("always-expected-groups"));
        when(connectAddOnUserProvisioningService.getDefaultProductGroupsOneOrMoreExpected()).thenReturn(Sets.newHashSet("one-or-more-expected-groups"));

        crowdAddOnUserService.getOrCreateUserName(ADDON_KEY, ADDON_NAME);

        verify(connectAddOnUserGroupProvisioningService).ensureUserIsInGroup(ADDON_USERNAME, ADDON_USER_GROUP_KEY);
        verify(connectAddOnUserGroupProvisioningService).ensureUserIsInGroup(ADDON_USERNAME, "always-expected-groups");
        verify(connectAddOnUserGroupProvisioningService).ensureUserIsInGroup(ADDON_USERNAME, "one-or-more-expected-groups");
    }

    @Test
    public void getOrCreateUserNameAddsPreExistingUserToAddonGroupOnly() throws Exception
    {
        when(connectCrowdService.createOrEnableUser(anyString(), anyString(), anyString(),
                any(PasswordCredential.class), anyMap())).thenReturn(new UserCreationResult(user, PRE_EXISTING));

        when(connectAddOnUserProvisioningService.getDefaultProductGroupsAlwaysExpected()).thenReturn(Sets.newHashSet("always-expected-groups"));
        when(connectAddOnUserProvisioningService.getDefaultProductGroupsOneOrMoreExpected()).thenReturn(Sets.newHashSet("one-or-more-expected-groups"));

        crowdAddOnUserService.getOrCreateUserName(ADDON_KEY, ADDON_NAME);

        verify(connectAddOnUserGroupProvisioningService, times(1)).ensureUserIsInGroup(anyString(), anyString());
        verify(connectAddOnUserGroupProvisioningService).ensureUserIsInGroup(ADDON_USERNAME, ADDON_USER_GROUP_KEY);
    }

    @Test
    public void getOrCreateUserNameDoesNotInvalidateSessionsWhenUserIsNew() throws Exception
    {
        crowdAddOnUserService.getOrCreateUserName(ADDON_KEY, ADDON_NAME);

        verify(connectCrowdService, never()).invalidateSessions(ADDON_USERNAME);
    }

    @Test
    public void getOrCreateUserNameInvalidatesSessionsForPreExistingUser() throws Exception
    {
        when(connectCrowdService.createOrEnableUser(anyString(), anyString(), anyString(),
                any(PasswordCredential.class), anyMap())).thenReturn(new UserCreationResult(user, PRE_EXISTING));

        crowdAddOnUserService.getOrCreateUserName(ADDON_KEY, ADDON_NAME);

        verify(connectCrowdService).invalidateSessions(ADDON_USERNAME);
    }
}