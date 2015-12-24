package it.com.atlassian.plugin.connect.jira.auth;

import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.permission.Permission;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.connect.spi.auth.user.ConnectUserService;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.util.auth.TestAuthenticator;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import it.com.atlassian.plugin.connect.jira.util.JiraTestUtil;
import org.junit.Test;
import org.junit.runner.RunWith;

@Application("jira")
@RunWith(AtlassianPluginsTestRunner.class)
public class JiraPermissionScopeInstallTest extends AbstractJiraPermissionScopeTest
{

    public JiraPermissionScopeInstallTest(ConnectUserService connectUserService,
                                          PermissionManager permissionManager,
                                          ProjectService projectService,
                                          ProjectRoleService projectRoleService,
                                          UserManager userManager,
                                          TestPluginInstaller testPluginInstaller,
                                          TestAuthenticator testAuthenticator,
                                          JiraTestUtil jiraTestUtil)
    {
        super(connectUserService,
                permissionManager,
                projectService,
                projectRoleService,
                userManager,
                testPluginInstaller,
                testAuthenticator,
                jiraTestUtil);
    }

    @Test
    public void addonIsMadeAdminOfExistingProjects() throws Exception
    {
        checkHasPermissionForAllProjectsAfterInstall(getProjectAdminAddon(), Permission.PROJECT_ADMIN);
    }

    @Test
    public void addonIsMadeAdminOfNewProject() throws Exception
    {
        checkHasPermissionForNewProjectAfterInstall(getProjectAdminAddon(), Permission.PROJECT_ADMIN);
    }

    @Test
    public void addonCanCreateIssuesInExistingProjects() throws Exception
    {
        checkHasPermissionForAllProjectsAfterInstall(getWriteAddon(), Permission.CREATE_ISSUE);
    }

    @Test
    public void addonCanCreateIssueInNewProject() throws Exception
    {
        checkHasPermissionForNewProjectAfterInstall(getWriteAddon(), Permission.CREATE_ISSUE);
    }

    @Test
    public void addonCanUpdateIssuesInExistingProjects() throws Exception
    {
        checkHasPermissionForAllProjectsAfterInstall(getWriteAddon(), Permission.EDIT_ISSUE);
    }

    @Test
    public void addonCanUpdateIssueInNewProject() throws Exception
    {
        checkHasPermissionForNewProjectAfterInstall(getWriteAddon(), Permission.EDIT_ISSUE);
    }

    @Test
    public void addonCanDeleteIssuesInExistingProjects() throws Exception
    {
        checkHasPermissionForAllProjectsAfterInstall(getDeleteAddon(), Permission.DELETE_ISSUE);
    }

    @Test
    public void addonCanDeleteIssueInNewProject() throws Exception
    {
        checkHasPermissionForNewProjectAfterInstall(getDeleteAddon(), Permission.DELETE_ISSUE);
    }

    @Test
    public void addonCannotEditIssuesInExistingProjects() throws Exception
    {
        checkHasPermissionForAllProjectsAfterInstall(getReadAddon(), Permission.EDIT_ISSUE);
    }

    @Test
    public void addonCannotEditIssueInNewProject() throws Exception
    {
        checkHasPermissionForNewProjectAfterInstall(getReadAddon(), Permission.EDIT_ISSUE);
    }
}
