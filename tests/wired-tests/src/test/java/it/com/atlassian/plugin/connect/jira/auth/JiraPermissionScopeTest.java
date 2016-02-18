package it.com.atlassian.plugin.connect.jira.auth;

import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.permission.Permission;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleActor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.spi.auth.user.ConnectUserService;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.util.auth.TestAuthenticator;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import it.com.atlassian.plugin.connect.jira.util.JiraTestUtil;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Application("jira")
@RunWith(AtlassianPluginsTestRunner.class)
public class JiraPermissionScopeTest extends AbstractJiraPermissionScopeTest
{

    public JiraPermissionScopeTest(ConnectUserService connectUserService,
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
    public void projectPermissionConfigurationIsRemovedAfterReinstallWithDowngradedScope() throws Exception
    {
        Plugin plugin = null;
        try
        {
            plugin = installPlugin(getProjectAdminAddon());
            uninstallPlugin(plugin);
            plugin = installPlugin(getReadAddon());

            List<String> projectAdminErrors = permissionsForAllProjects(Permission.PROJECT_ADMIN, false, plugin);
            assertTrue(StringUtils.join(projectAdminErrors, '\n'), projectAdminErrors.isEmpty());
        }
        finally
        {
            uninstallPlugin(plugin);
        }
    }

    @Test
    public void addonEnablementChangesPreserveCustomConfiguration() throws Exception
    {
        Plugin plugin = null;
        try
        {
            plugin = installPlugin(getProjectAdminAddon());

            Project project = jiraTestUtil.createProject();
            ApplicationUser addonUser = getAddonUser();

            SimpleErrorCollection errorCollection = new SimpleErrorCollection();
            ProjectRole projectRole = getProjectRoleService().getProjectRoleByName("atlassian-addons-project-access", errorCollection);
            getProjectRoleService().removeActorsFromProjectRole(
                    Collections.singleton(addonUser.getKey()),
                    projectRole,
                    project,
                    ProjectRoleActor.USER_ROLE_ACTOR_TYPE,
                    errorCollection);

            boolean addonCannotAdministerProject = getPermissionManager().hasPermission(Permissions.PROJECT_ADMIN, project, addonUser, false);
            assertFalse("Add-on user " + addonUser.getName() + " should not have administer permission for project " + project.getKey(), addonCannotAdministerProject);

            disablePlugin(plugin);
            enablePlugin(plugin);

            boolean addonStillCannotAdministerProject = getPermissionManager().hasPermission(Permissions.PROJECT_ADMIN, project, addonUser, false);
            assertFalse("Add-on user " + addonUser.getName() + " should not have administer permission for project " + project.getKey(), addonStillCannotAdministerProject);
        }
        finally
        {
            uninstallPlugin(plugin);
        }
    }
}
