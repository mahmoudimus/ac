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
public class JiraPermissionScopeTransitionNewTest extends AbstractJiraPermissionScopeTest {

    public JiraPermissionScopeTransitionNewTest(ConnectUserService connectUserService,
                                                PermissionManager permissionManager,
                                                ProjectService projectService,
                                                ProjectRoleService projectRoleService,
                                                UserManager userManager,
                                                TestPluginInstaller testPluginInstaller,
                                                TestAuthenticator testAuthenticator,
                                                JiraTestUtil jiraTestUtil) {
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
    public void testAdminToProjectAdminTransitionForNewProjects() throws Exception {
        checkHasPermissionForNewProjectAfterTransition(getAdminAddon(), getProjectAdminAddon(), Permission.PROJECT_ADMIN);
    }

    @Test
    public void testProjectAdminToDeleteTransitionForNewProjects() throws Exception {
        checkHasPermissionForNewProjectAfterTransition(getProjectAdminAddon(), getDeleteAddon(), Permission.DELETE_ISSUE);
    }

    @Test
    public void testDeleteToWriteTransitionForNewProjects() throws Exception {
        checkHasPermissionForNewProjectAfterTransition(getDeleteAddon(), getWriteAddon(), Permission.EDIT_ISSUE);
    }

    @Test
    public void testWriteToReadTransitionForNewProjects() throws Exception {
        checkHasPermissionForNewProjectAfterTransition(getWriteAddon(), getReadAddon(), Permission.EDIT_ISSUE);
    }
}
