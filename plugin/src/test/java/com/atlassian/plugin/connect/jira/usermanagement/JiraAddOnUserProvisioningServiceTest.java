package com.atlassian.plugin.connect.jira.usermanagement;

import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserGroupProvisioningService;
import com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserInitException;
import com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserProvisioningService;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserKey;

import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JiraAddOnUserProvisioningServiceTest
{
    private static UserKey USER_KEY = new UserKey("aaab-bccd-eef-ggh");
    private static String ADDONS_ADMIN_GROUP = "atlassian-addons-admin";

    @Mock private GlobalPermissionManager jiraPermissionManager;
    @Mock private PermissionSchemeManager permissionSchemeManager;
    @Mock private ProjectManager projectManager;
    @Mock private ProjectRoleService projectRoleService;
    @Mock private UserManager userManager;
    @Mock private ConnectAddOnUserGroupProvisioningService connectAddOnUserGroupProvisioningService;
    @Mock private PermissionManager jiraProjectPermissionManager;
    @Mock private ApplicationUser adminUser;

    private TransactionTemplate transactionTemplate = new TransactionTemplate()
    {
        @Override
        public <T> T execute(TransactionCallback<T> action)
        {
            return action.doInTransaction();
        }
    };
    private JiraAddOnUserProvisioningService provisioningService;

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
                                                                   jiraProjectPermissionManager);

    }

    @Test
    public void testMissingAdminPermissionReturnsCorrectErrorCode() throws ApplicationNotFoundException,
        OperationFailedException, ApplicationPermissionException
    {
        when(userManager.getUserByKey(USER_KEY.getStringValue())).thenReturn(adminUser);

        when(connectAddOnUserGroupProvisioningService.ensureGroupExists(ADDONS_ADMIN_GROUP)).thenReturn(false);

        when(jiraPermissionManager.getGroupsWithPermission(Permissions.ADMINISTER)).thenReturn(Collections.EMPTY_LIST);

        Set<ScopeName> previousScopes = Sets.newHashSet();
        Set<ScopeName> newScopes = Sets.newHashSet(ScopeName.ADMIN);

        try
        {
            provisioningService.provisionAddonUserForScopes(USER_KEY, previousScopes, newScopes);
            fail("Provisioning addon should not have succeeded");
        }
        catch (ConnectAddOnUserInitException exception)
        {
            assertEquals(exception.getI18nKey(), ConnectAddOnUserProvisioningService.ADDON_ADMINS_MISSING_PERMISSION);
        }
    }
}
