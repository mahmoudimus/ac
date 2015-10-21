package it.com.atlassian.plugin.connect.jira.auth;

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
public class JiraPermissionScopeTransitionExistingTest extends AbstractJiraPermissionScopeTest
{

    public JiraPermissionScopeTransitionExistingTest(ConnectUserService connectUserService,
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
        checkHasPermissionForAllProjectsAfterTransition(getWriteAddOn(), getReadAddOn(), Permission.EDIT_ISSUE);
    }
}
