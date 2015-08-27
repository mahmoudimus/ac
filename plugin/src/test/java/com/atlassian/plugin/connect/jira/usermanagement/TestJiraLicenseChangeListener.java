package com.atlassian.plugin.connect.jira.usermanagement;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.atlassian.application.api.ApplicationKey;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.model.user.User;
import com.atlassian.fugue.Option;
import com.atlassian.jira.application.ApplicationAuthorizationService;
import com.atlassian.jira.application.ApplicationRoleManager;
import com.atlassian.jira.license.LicenseChangedEvent;
import com.atlassian.jira.license.LicenseDetails;
import com.atlassian.jira.license.MockLicensedApplications;
import com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserGroupProvisioningService;
import com.atlassian.plugin.connect.crowd.usermanagement.ConnectAddOnUsers;

import com.google.common.collect.ImmutableSet;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.some;
import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class TestJiraLicenseChangeListener
{
    @Mock
    private JiraLicenseChangeListener jiraLicenseChangeListener;
    @Mock
    private ApplicationAuthorizationService applicationAuthorizationService;
    @Mock
    private ApplicationRoleManager applicationRoleManager;
    @Mock
    private ConnectAddOnUsers connectAddOnUsers;
    @Mock
    private ConnectAddOnUserGroupProvisioningService connectAddOnUserGroupProvisioningService;
    private User polonius;
    private User charles;

    @Before
    public void beforeEach()
    {
        initMocks(this);

        when(applicationAuthorizationService.rolesEnabled()).thenReturn(true);
        polonius = mock(User.class);
        when(polonius.getName()).thenReturn("honestman");
        charles = mock(User.class);
        when(charles.getName()).thenReturn("cheidsieck");

        jiraLicenseChangeListener = new JiraLicenseChangeListener(applicationRoleManager, connectAddOnUsers, connectAddOnUserGroupProvisioningService, applicationAuthorizationService);
        when(connectAddOnUsers.getAddonUsers()).thenReturn(asList(polonius, charles));
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void onLicenseChangedIgnoresBrandNewLicense() throws Exception
    {
        LicenseChangedEvent event = mockLicenseKeys(Collections.<String>emptySet(), ImmutableSet.of("jira-for-skaters"));
        jiraLicenseChangeListener.onLicenseChanged(event);

        verify(applicationRoleManager, never()).getDefaultGroups(any(ApplicationKey.class));
        verify(connectAddOnUserGroupProvisioningService, never()).ensureUserIsInGroups(anyString(), anySet());
        verify(connectAddOnUserGroupProvisioningService, never()).ensureUserIsInGroup(anyString(), anyString());
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void onLicenseChangedIgnoresRevokedLicense() throws Exception
    {
        LicenseChangedEvent event = mockLicenseKeys(ImmutableSet.of("jira-for-lepidopterists"), Collections.<String>emptySet());
        jiraLicenseChangeListener.onLicenseChanged(event);

        verify(applicationRoleManager, never()).getDefaultGroups(any(ApplicationKey.class));
        verify(connectAddOnUserGroupProvisioningService, never()).ensureUserIsInGroups(anyString(), anySet());
        verify(connectAddOnUserGroupProvisioningService, never()).ensureUserIsInGroup(anyString(), anyString());
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void onLicenseChangedIgnoresEmptyLicenseEvent() throws Exception
    {
        LicenseChangedEvent event = mockLicenseKeys(Collections.<String>emptySet(), Collections.<String>emptySet());
        jiraLicenseChangeListener.onLicenseChanged(event);

        verify(applicationRoleManager, never()).getDefaultGroups(any(ApplicationKey.class));
        verify(connectAddOnUserGroupProvisioningService, never()).ensureUserIsInGroups(anyString(), anySet());
        verify(connectAddOnUserGroupProvisioningService, never()).ensureUserIsInGroup(anyString(), anyString());
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void onLicenseChangedIsNoOpWhenRenaissanceIsOff() throws Exception
    {
        when(applicationAuthorizationService.rolesEnabled()).thenReturn(false);
        LicenseChangedEvent event = mockLicenseKeys(ImmutableSet.of("jira-for-arborists"), ImmutableSet.of("jira-for-arborists", "jira-for-cattle-rustlers"));
        jiraLicenseChangeListener.onLicenseChanged(event);

        verify(applicationRoleManager, never()).getDefaultGroups(any(ApplicationKey.class));
        verify(connectAddOnUserGroupProvisioningService, never()).ensureUserIsInGroups(anyString(), anySet());
        verify(connectAddOnUserGroupProvisioningService, never()).ensureUserIsInGroup(anyString(), anyString());
    }


    @Test
    public void onLicenseChangedAddsUsersToNewlyLicensedApplicationDefaultGroups() throws Exception
    {
        String oldAppKey = "jira-for-fishmongers";
        String newAppKey = "jira-for-viticulturists";
        LicenseChangedEvent event = mockLicenseKeys(
                ImmutableSet.of(oldAppKey), ImmutableSet.of(oldAppKey, newAppKey));
        Group newAppGroup = mock(Group.class);
        when(newAppGroup.getName()).thenReturn("viticulturists");
        when(applicationRoleManager.getDefaultGroups(any(ApplicationKey.class))).thenReturn(ImmutableSet.of(newAppGroup));

        jiraLicenseChangeListener.onLicenseChanged(event);

        verify(applicationRoleManager).getDefaultGroups(ApplicationKey.valueOf(newAppKey));
        verify(applicationRoleManager, never()).getDefaultGroups(ApplicationKey.valueOf(oldAppKey));
        verify(connectAddOnUserGroupProvisioningService).ensureUserIsInGroups(polonius.getName(), ImmutableSet.of(newAppGroup.getName()));
        verify(connectAddOnUserGroupProvisioningService).ensureUserIsInGroups(charles.getName(), ImmutableSet.of(newAppGroup.getName()));
    }

    // ensureUserIsInGroup throws for one of the users
    // * The rest of the users still get added

    @SuppressWarnings ("unchecked")
    private LicenseChangedEvent mockLicenseKeys(Set<String> oldKeys, Set<String> newKeys)
    {
        LicenseDetails oldLicenseDetails = mock(LicenseDetails.class);
        LicenseDetails newLicenseDetails = mock(LicenseDetails.class);

        HashSet<ApplicationKey> oldApps = new HashSet<>();
        HashSet<ApplicationKey> newApps = new HashSet<>();

        for (String key : oldKeys)
        {
            oldApps.add(ApplicationKey.valueOf(key));
        }
        when(oldLicenseDetails.getLicensedApplications()).thenReturn(
                new MockLicensedApplications(oldApps));

        for (String key : newKeys)
        {
            newApps.add(ApplicationKey.valueOf(key));
        }
        when(newLicenseDetails.getLicensedApplications()).thenReturn(
                new MockLicensedApplications(newApps));

        Option oldDetailsOption = oldKeys.isEmpty() ? none() : some(oldLicenseDetails);
        Option newDetailsOption = newKeys.isEmpty() ? none() : some(newLicenseDetails);

        return new LicenseChangedEvent(oldDetailsOption, newDetailsOption);
    }
}