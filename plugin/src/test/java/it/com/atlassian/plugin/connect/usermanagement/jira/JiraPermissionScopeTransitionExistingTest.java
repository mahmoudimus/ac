package it.com.atlassian.plugin.connect.usermanagement.jira;

import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.permission.Permission;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserService;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import it.com.atlassian.plugin.connect.TestAuthenticator;
import org.junit.Test;
import org.junit.runner.RunWith;

@Application("jira")
@RunWith(AtlassianPluginsTestRunner.class)
public class JiraPermissionScopeTransitionExistingTest extends AbstractJiraPermissionScopeTest
{

    public JiraPermissionScopeTransitionExistingTest(ConnectAddOnUserService connectAddOnUserService,
                                                     PermissionManager permissionManager, ProjectService projectService,
                                                     ProjectRoleService projectRoleService, UserManager userManager,
                                                     TestPluginInstaller testPluginInstaller, TestAuthenticator testAuthenticator)
    {
        super(connectAddOnUserService, permissionManager, projectService, projectRoleService, userManager, testPluginInstaller, testAuthenticator);
    }

    @Test
    public void testAdminToProjectAdminTransitionForExistingProjects() throws Exception
    {
        checkHasPermissionForAllProjectsAfterTransition(getAdminAddOn(), getProjectAdminAddOn(), Permission.PROJECT_ADMIN);
    }

    @Test
    public void testProjectAdminToDeleteTransitionForExistingProjects() throws Exception
    {
        checkHasPermissionForAllProjectsAfterTransition(getProjectAdminAddOn(), getDeleteAddOn(), Permission.DELETE_ISSUE);
    }

    @Test
    public void testDeleteToWriteTransitionForExistingProjects() throws Exception
    {
        checkHasPermissionForAllProjectsAfterTransition(getDeleteAddOn(), getWriteAddOn(), Permission.EDIT_ISSUE);
    }

    @Test
    public void testWriteToReadTransitionForExistingProjects() throws Exception
    {
        checkHasNoPermissionForAnyProjectAfterTransition(getWriteAddOn(), getReadAddOn(), Permission.EDIT_ISSUE);
    }
}
