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
public class JiraPermissionScopeInstallTest extends AbstractJiraPermissionScopeTest
{
    public JiraPermissionScopeInstallTest(ConnectAddOnUserService connectAddOnUserService,
                                          PermissionManager permissionManager, ProjectService projectService,
                                          ProjectRoleService projectRoleService, UserManager userManager,
                                          TestPluginInstaller testPluginInstaller, TestAuthenticator testAuthenticator)
    {
       super(connectAddOnUserService, permissionManager, projectService, projectRoleService, userManager, testPluginInstaller, testAuthenticator);
    }

    @Test
    public void addonIsMadeAdminOfExistingProjects() throws Exception
    {
        checkHasPermissionForAllProjectsAfterInstall(getProjectAdminAddOn(), Permission.PROJECT_ADMIN);
    }

    @Test
    public void addonIsMadeAdminOfNewProject() throws Exception
    {
        checkHasPermissionForNewProjectAfterInstall(getProjectAdminAddOn(), Permission.PROJECT_ADMIN);
    }

    @Test
    public void addonCanCreateIssuesInExistingProjects() throws Exception
    {
        checkHasPermissionForAllProjectsAfterInstall(getWriteAddOn(), Permission.CREATE_ISSUE);
    }

    @Test
    public void addonCanCreateIssueInNewProject() throws Exception
    {
        checkHasPermissionForNewProjectAfterInstall(getWriteAddOn(), Permission.CREATE_ISSUE);
    }

    @Test
    public void addonCanUpdateIssuesInExistingProjects() throws Exception
    {
        checkHasPermissionForAllProjectsAfterInstall(getWriteAddOn(), Permission.EDIT_ISSUE);
    }

    @Test
    public void addonCanUpdateIssueInNewProject() throws Exception
    {
        checkHasPermissionForNewProjectAfterInstall(getWriteAddOn(), Permission.EDIT_ISSUE);
    }

    @Test
    public void addonCanDeleteIssuesInExistingProjects() throws Exception
    {
        checkHasPermissionForAllProjectsAfterInstall(getDeleteAddOn(), Permission.DELETE_ISSUE);
    }

    @Test
    public void addonCanDeleteIssueInNewProject() throws Exception
    {
        checkHasPermissionForNewProjectAfterInstall(getDeleteAddOn(), Permission.DELETE_ISSUE);
    }

    @Test
    public void addonCannotEditIssuesInExistingProjects() throws Exception
    {
        checkHasNoPermissionForAnyProjectAfterInstall(getReadAddOn(), Permission.EDIT_ISSUE);
    }

    @Test
    public void addonCannotEditIssueInNewProject() throws Exception
    {
        checkHasNoPermissionForNewProjectAfterInstall(getReadAddOn(), Permission.EDIT_ISSUE);
    }
}