package com.atlassian.plugin.connect.crowd.usermanagement;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.plugin.connect.crowd.spi.CrowdAddonUserProvisioningService;
import com.atlassian.plugin.connect.spi.HostProperties;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static com.atlassian.plugin.connect.crowd.usermanagement.ConnectAddonUserUtil.Constants.ADDON_USER_GROUP_KEY;
import static com.atlassian.plugin.connect.crowd.usermanagement.ConnectAddonUserUtil.buildConnectAddonUserAttribute;
import static com.atlassian.plugin.connect.crowd.usermanagement.ConnectAddonUserUtil.usernameForAddon;
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

public class TestCrowdAddonUserService {
    public static final String PRODUCT_KEY = "the-product";
    public static final String ADDON_KEY = "the-addon-key";
    public static final String ADDON_USERNAME = usernameForAddon(ADDON_KEY);
    public static final String ADDON_NAME = "The Addon";
    private CrowdAddonUserService crowdAddonUserService;

    @Mock
    private CrowdAddonUserProvisioningService crowdAddonUserProvisioningService;
    @Mock
    private ConnectAddonUserGroupProvisioningService connectAddonUserGroupProvisioningService;
    @Mock
    private ConnectCrowdService connectCrowdService;
    @Mock
    private HostProperties hostProperties;
    @Mock
    private User user;

    @Before
    public void beforeEach() {
        initMocks(this);
        when(user.getName()).thenReturn(ADDON_USERNAME);
        when(connectCrowdService.createOrEnableUser(anyString(), anyString(), anyString(),
                any(PasswordCredential.class), anyMap())).thenReturn(new UserCreationResult(user, NEWLY_CREATED));
        crowdAddonUserService = new CrowdAddonUserService(
                crowdAddonUserProvisioningService,
                connectAddonUserGroupProvisioningService,
                connectCrowdService,
                hostProperties);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void getOrCreateUserNameCreatesAddonUser() throws Exception {
        when(hostProperties.getKey()).thenReturn(PRODUCT_KEY);
        crowdAddonUserService.getOrCreateAddonUserName(ADDON_KEY, ADDON_NAME);

        verify(connectCrowdService).createOrEnableUser(ConnectAddonUserUtil.usernameForAddon(ADDON_KEY),
                ADDON_NAME, ConnectAddonUserUtil.Constants.ADDON_USER_EMAIL_ADDRESS,
                PasswordCredential.NONE, buildConnectAddonUserAttribute(PRODUCT_KEY));
    }

    @Test
    public void getOrCreateUserNameCreatesAddonGroup() throws Exception {
        crowdAddonUserService.getOrCreateAddonUserName(ADDON_KEY, ADDON_NAME);
        verify(connectAddonUserGroupProvisioningService).ensureGroupExists(ADDON_USER_GROUP_KEY);
    }

    @Test
    public void getOrCreateUserNameAddsAddonUserToGroupsWhenUserIsNew() throws Exception {
        when(crowdAddonUserProvisioningService.getDefaultProductGroupsAlwaysExpected()).thenReturn(Sets.newHashSet("always-expected-groups"));
        when(crowdAddonUserProvisioningService.getDefaultProductGroupsOneOrMoreExpected()).thenReturn(Sets.newHashSet("one-or-more-expected-groups"));

        crowdAddonUserService.getOrCreateAddonUserName(ADDON_KEY, ADDON_NAME);

        verify(connectAddonUserGroupProvisioningService).ensureUserIsInGroup(ADDON_USERNAME, ADDON_USER_GROUP_KEY);
        verify(connectAddonUserGroupProvisioningService).ensureUserIsInGroup(ADDON_USERNAME, "always-expected-groups");
        verify(connectAddonUserGroupProvisioningService).ensureUserIsInGroup(ADDON_USERNAME, "one-or-more-expected-groups");
    }

    @Test
    public void getOrCreateUserNameAddsPreExistingUserToAddonGroupOnly() throws Exception {
        when(connectCrowdService.createOrEnableUser(anyString(), anyString(), anyString(),
                any(PasswordCredential.class), anyMap())).thenReturn(new UserCreationResult(user, PRE_EXISTING));

        when(crowdAddonUserProvisioningService.getDefaultProductGroupsAlwaysExpected()).thenReturn(Sets.newHashSet("always-expected-groups"));
        when(crowdAddonUserProvisioningService.getDefaultProductGroupsOneOrMoreExpected()).thenReturn(Sets.newHashSet("one-or-more-expected-groups"));

        crowdAddonUserService.getOrCreateAddonUserName(ADDON_KEY, ADDON_NAME);

        verify(connectAddonUserGroupProvisioningService, times(1)).ensureUserIsInGroup(anyString(), anyString());
        verify(connectAddonUserGroupProvisioningService).ensureUserIsInGroup(ADDON_USERNAME, ADDON_USER_GROUP_KEY);
    }

    @Test
    public void getOrCreateUserNameDoesNotInvalidateSessionsWhenUserIsNew() throws Exception {
        crowdAddonUserService.getOrCreateAddonUserName(ADDON_KEY, ADDON_NAME);

        verify(connectCrowdService, never()).invalidateSessions(ADDON_USERNAME);
    }

    @Test
    public void getOrCreateUserNameInvalidatesSessionsForPreExistingUser() throws Exception {
        when(connectCrowdService.createOrEnableUser(anyString(), anyString(), anyString(),
                any(PasswordCredential.class), anyMap())).thenReturn(new UserCreationResult(user, PRE_EXISTING));

        crowdAddonUserService.getOrCreateAddonUserName(ADDON_KEY, ADDON_NAME);

        verify(connectCrowdService).invalidateSessions(ADDON_USERNAME);
    }
}
