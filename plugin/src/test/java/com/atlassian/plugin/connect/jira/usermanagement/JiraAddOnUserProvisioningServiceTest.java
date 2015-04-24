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
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserGroupProvisioningService;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserInitException;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserProvisioningService;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(MockitoJUnitRunner.class)
public class JiraAddOnUserProvisioningServiceTest
{
    private static String USERNAME = "addon-blaah";
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
        Mockito.when(userManager.getUserByName(USERNAME)).thenReturn(adminUser);

        Mockito.when(connectAddOnUserGroupProvisioningService.ensureGroupExists(ADDONS_ADMIN_GROUP)).thenReturn(false);

        Mockito.when(jiraPermissionManager.getGroupsWithPermission(Permissions.ADMINISTER)).thenReturn(Collections.EMPTY_LIST);

        Set<ScopeName> previousScopes = Sets.newHashSet();
        Set<ScopeName> newScopes = Sets.newHashSet(ScopeName.ADMIN);

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
}
