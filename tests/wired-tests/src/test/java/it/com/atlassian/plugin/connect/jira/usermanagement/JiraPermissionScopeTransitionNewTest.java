package it.com.atlassian.plugin.connect.jira.usermanagement;

import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.permission.Permission;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.connect.spi.user.ConnectUserService;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.util.auth.TestAuthenticator;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;

import it.com.atlassian.plugin.connect.jira.util.JiraTestUtil;
import org.junit.Test;
import org.junit.runner.RunWith;

@Application("jira")
@RunWith(AtlassianPluginsTestRunner.class)
public class JiraPermissionScopeTransitionNewTest extends AbstractJiraPermissionScopeTest
{

    public JiraPermissionScopeTransitionNewTest(ConnectUserService connectUserService,
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
        checkHasPermissionForNewProjectAfterTransition(getWriteAddOn(), getReadAddOn(), Permission.EDIT_ISSUE);
    }
}
