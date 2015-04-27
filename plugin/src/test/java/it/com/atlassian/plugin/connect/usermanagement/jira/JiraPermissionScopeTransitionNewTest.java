package it.com.atlassian.plugin.connect.usermanagement.jira;

import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.compatibility.bridge.project.ProjectServiceBridge;
import com.atlassian.jira.permission.Permission;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserService;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;

import org.junit.Test;
import org.junit.runner.RunWith;

import it.com.atlassian.plugin.connect.TestAuthenticator;

@Application("jira")
@RunWith(AtlassianPluginsTestRunner.class)
public class JiraPermissionScopeTransitionNewTest extends AbstractJiraPermissionScopeTest
{

    public JiraPermissionScopeTransitionNewTest(ConnectAddOnUserService connectAddOnUserService,
                                                PermissionManager permissionManager, ProjectService projectService, ProjectServiceBridge projectServiceBridge,
                                                ProjectRoleService projectRoleService, UserManager userManager,
                                                TestPluginInstaller testPluginInstaller, TestAuthenticator testAuthenticator)
    {
        super(connectAddOnUserService, permissionManager, projectService, projectServiceBridge, projectRoleService, userManager, testPluginInstaller, testAuthenticator);
    }

    @Test
    public void testAdminToProjectAdminTransitionForNewProjects() throws Exception
    {
        checkHasPermissionForNewProjectAfterTransition(getAdminAddOn(), getProjectAdminAddOn(), Permission.PROJECT_ADMIN);
    }

    @Test
    public void testProjectAdminToDeleteTransitionForNewProjects() throws Exception
    {
        checkHasPermissionForNewProjectAfterTransition(getProjectAdminAddOn(), getDeleteAddOn(), Permission.DELETE_ISSUE);
    }

    @Test
    public void testDeleteToWriteTransitionForNewProjects() throws Exception
    {
        checkHasPermissionForNewProjectAfterTransition(getDeleteAddOn(), getWriteAddOn(), Permission.EDIT_ISSUE);
    }

    @Test
    public void testWriteToReadTransitionForNewProjects() throws Exception
    {
        checkHasNoPermissionForNewProjectAfterTransition(getWriteAddOn(), getReadAddOn(), Permission.EDIT_ISSUE);
    }
}
