package com.atlassian.plugin.connect.jira.usermanagement;

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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        polonius = mock(User.class);
        when(polonius.getName()).thenReturn("honestman");
        charles = mock(User.class);
        when(charles.getName()).thenReturn("cheidsieck");

        jiraLicenseChangeListener = new JiraLicenseChangeListener(applicationRoleManager, connectAddOnUsers, connectAddOnUserGroupProvisioningService, applicationAuthorizationService);
        when(connectAddOnUsers.getAddonUsers()).thenReturn(asList(polonius, charles));
    }

    // One or both of the license details are empty
    // * It doesn't do anything

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

        verify(connectAddOnUserGroupProvisioningService).ensureUserIsInGroup(charles.getName(), newAppGroup.getName());
        verify(connectAddOnUserGroupProvisioningService).ensureUserIsInGroup(polonius.getName(), newAppGroup.getName());
    }

    // ensureUserIsInGroup throws for one of the users
    // * The rest of the users still get added

    @SuppressWarnings ("unchecked")
    private LicenseChangedEvent mockLicenseKeys(Set<String> oldKeys, Set<String> newKeys)
    {
        LicenseChangedEvent event = mock(LicenseChangedEvent.class);
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
        when(event.getNewLicenseDetails()).thenReturn(oldDetailsOption);
        when(event.getNewLicenseDetails()).thenReturn(newDetailsOption);

        return event;
    }
}