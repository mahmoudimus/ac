package com.atlassian.plugin.connect.jira.usermanagement;

import java.util.Collections;
import java.util.Set;

import com.atlassian.application.api.ApplicationKey;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.jira.application.ApplicationAuthorizationService;
import com.atlassian.jira.application.ApplicationRoleManager;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserGroupProvisioningService;
import com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserInitException;
import com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserProvisioningService;
import com.atlassian.plugin.connect.crowd.permissions.ConnectCrowdPermissions;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;

import org.hamcrest.collection.IsEmptyCollection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JiraAddOnUserProvisioningServiceTest
{
    private static String USERNAME = "addon-blaah";
    private static String ADDONS_ADMIN_GROUP = "atlassian-addons-admin";
    private static String REN_GROUP = "ren-users";

    @Mock private GlobalPermissionManager jiraPermissionManager;
    @Mock private PermissionSchemeManager permissionSchemeManager;
    @Mock private ProjectManager projectManager;
    @Mock private ProjectRoleService projectRoleService;
    @Mock private UserManager userManager;
    @Mock private ConnectAddOnUserGroupProvisioningService connectAddOnUserGroupProvisioningService;
    @Mock private PermissionManager jiraProjectPermissionManager;
    @Mock private ApplicationUser adminUser;
    @Mock private ApplicationAuthorizationService applicationAuthorizationService;
    @Mock private ApplicationRoleManager applicationRoleManager;
    private ApplicationKey applicationKey;
    @Mock private Group group;
    @Mock private ConnectCrowdPermissions connectCrowdPermissions;

    private TransactionTemplate transactionTemplate = new TransactionTemplate()
    {
        @Override
        public <T> T execute(TransactionCallback<T> action)
        {
            return action.doInTransaction();
        }
    };
    private JiraAddOnUserProvisioningService provisioningService;
    private Set<ApplicationKey> applicationKeys;
    private Set<Group> groups;

    @Before
    public void setup()
    {
        provisioningService = new JiraAddOnUserProvisioningService(jiraPermissionManager,
                projectManager,
                userManager,
                permissionSchemeManager,
                projectRoleService,
                connectAddOnUserGroupProvisioningService,
                transactionTemplate,
                jiraProjectPermissionManager,
                applicationAuthorizationService,
                applicationRoleManager,
                connectCrowdPermissions);

        groups = newHashSet();
        applicationKeys = newHashSet();
        applicationKey = ApplicationKey.valueOf("my-test");
        applicationKeys.add(applicationKey);
        groups.add(group);
        when(group.getName()).thenReturn(REN_GROUP);
    }

    @Test
    public void testMissingAdminPermissionReturnsCorrectErrorCode()
            throws ApplicationNotFoundException,
            OperationFailedException, ApplicationPermissionException, InvalidAuthenticationException
    {
        when(userManager.getUserByName(USERNAME)).thenReturn(adminUser);

        when(connectAddOnUserGroupProvisioningService.ensureGroupExists(ADDONS_ADMIN_GROUP)).thenReturn(false);

        when(jiraPermissionManager.getGroupsWithPermission(Permissions.ADMINISTER)).thenReturn(Collections.EMPTY_LIST);


        Set<ScopeName> previousScopes = newHashSet();
        Set<ScopeName> newScopes = newHashSet(ScopeName.ADMIN);

        try
        {
            provisioningService.provisionAddonUserForScopes(USERNAME, previousScopes, newScopes);
            fail("Provisioning addon should not have succeeded");
        }
        catch (ConnectAddOnUserInitException exception)
        {
            assertEquals(exception.getI18nKey(), ConnectAddOnUserProvisioningService.ADDON_ADMINS_MISSING_PERMISSION);
        }
    }

    @Test
    public void testAdminGrantProvidesCorrectProductAndApplicationIdInRenaissance()
            throws ApplicationPermissionException,
            OperationFailedException, ApplicationNotFoundException, InvalidAuthenticationException
    {
        when(userManager.getUserByName(USERNAME)).thenReturn(adminUser);
        when(connectAddOnUserGroupProvisioningService.ensureGroupExists(ADDONS_ADMIN_GROUP)).thenReturn(true);
        when(connectCrowdPermissions.giveAdminPermission(anyString(), anyString(), anyString())).thenReturn(ConnectCrowdPermissions.GrantResult.REMOTE_GRANT_SUCCEEDED);
        when(applicationAuthorizationService.rolesEnabled()).thenReturn(true);

        Set<ScopeName> previousScopes = newHashSet();
        Set<ScopeName> newScopes = newHashSet(ScopeName.ADMIN);

        provisioningService.provisionAddonUserForScopes(USERNAME, previousScopes, newScopes);

        verify(connectCrowdPermissions).giveAdminPermission(anyString(), eq("jira"), eq("jira-admin"));
    }

    @Test
    public void testAdminGrantProvidesCorrectProductAndApplicationIdInDarkAges()
            throws ApplicationPermissionException,
            OperationFailedException, ApplicationNotFoundException, InvalidAuthenticationException
    {
        when(userManager.getUserByName(USERNAME)).thenReturn(adminUser);
        when(connectAddOnUserGroupProvisioningService.ensureGroupExists(ADDONS_ADMIN_GROUP)).thenReturn(true);
        when(connectCrowdPermissions.giveAdminPermission(anyString(), anyString(), anyString())).thenReturn(ConnectCrowdPermissions.GrantResult.REMOTE_GRANT_SUCCEEDED);
        when(applicationAuthorizationService.rolesEnabled()).thenReturn(false);

        Set<ScopeName> previousScopes = newHashSet();
        Set<ScopeName> newScopes = newHashSet(ScopeName.ADMIN);

        provisioningService.provisionAddonUserForScopes(USERNAME, previousScopes, newScopes);

        verify(connectCrowdPermissions).giveAdminPermission(anyString(), eq("jira"), eq("jira"));
    }

    @Test
    public void testGetDefaultProductGroupsOneOrMoreExpectedRenaissance()
    {
        when(applicationAuthorizationService.rolesEnabled()).thenReturn(true);
        when(applicationRoleManager.getDefaultApplicationKeys()).thenReturn(applicationKeys);
        when(applicationRoleManager.getDefaultGroups(any(ApplicationKey.class))).thenReturn(groups);

        assertThat(provisioningService.getDefaultProductGroupsOneOrMoreExpected(), containsInAnyOrder(REN_GROUP));
        verify(applicationRoleManager).getDefaultGroups(applicationKey);
    }

    @Test
    public void testGetDefaultProductGroupsOneOrMoreExpectedDarkAges()
    {
        when(applicationAuthorizationService.rolesEnabled()).thenReturn(false);

        assertThat(provisioningService.getDefaultProductGroupsOneOrMoreExpected(), containsInAnyOrder("jira-users", "users"));
        verify(applicationRoleManager, never()).getDefaultApplicationKeys();
        verify(applicationRoleManager, never()).getDefaultGroups(any(ApplicationKey.class));
    }

    @Test
    public void testGetDefaultProductGroupsAlwaysExpectedReturnsEmptySet()
    {
        assertThat(provisioningService.getDefaultProductGroupsAlwaysExpected(), empty());
    }
}
