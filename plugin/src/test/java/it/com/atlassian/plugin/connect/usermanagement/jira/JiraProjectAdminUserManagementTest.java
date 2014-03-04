package it.com.atlassian.plugin.connect.usermanagement.jira;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddOnUserService;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Set;

import static com.atlassian.plugin.connect.modules.beans.AuthenticationBean.newAuthenticationBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.LifecycleBean.newLifecycleBean;
import static org.junit.Assert.assertTrue;

@Application("jira")
@RunWith(AtlassianPluginsTestRunner.class)
public class JiraProjectAdminUserManagementTest
{
    private static final String ADMIN = "admin";
    private static String ADDON_KEY = "project-admin-addon";
    private static final String INSTALLED = "/installed";

    private final ConnectAddOnUserService connectAddOnUserService;
    private final PermissionManager permissionManager;
    private final ProjectService projectService;
    private final UserManager userManager;
    private final TestPluginInstaller testPluginInstaller;
    private static Set<ScopeName> NO_SCOPES = ImmutableSet.of();

    private ConnectAddonBean projectAdminAddOn;

    public JiraProjectAdminUserManagementTest(ConnectAddOnUserService connectAddOnUserService,
                                              PermissionManager permissionManager, ProjectService projectService, UserManager userManager,
                                              TestPluginInstaller testPluginInstaller)
    {
        this.connectAddOnUserService = connectAddOnUserService;
        this.permissionManager = permissionManager;
        this.projectService = projectService;
        this.userManager = userManager;
        this.testPluginInstaller = testPluginInstaller;
    }

    @Before
    public void setUp()
    {
        this.projectAdminAddOn = newConnectAddonBean()
                .withName("JIRA Project Admin Add-on")
                .withKey(ADDON_KEY)
                .withAuthentication(newAuthenticationBean().withType(AuthenticationType.JWT).build())
                .withLifecycle(
                        newLifecycleBean()
                                .withInstalled(INSTALLED)
                                .build()
                )
                .withScopes(Sets.newHashSet(ScopeName.PROJECT_ADMIN))
                .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(ADDON_KEY))
                .build();
    }

    @Test
    public void addonIsMadeAdminOfExistingProject() throws Exception
    {
        Plugin plugin = null;
        try
        {
            plugin = testPluginInstaller.installPlugin(projectAdminAddOn);

            User addonUser = connectAddOnUserService.getUserByAddOnKey(ADDON_KEY);

            List<Project> allProjects = projectService.getAllProjects(addonUser).getReturnedValue();

            List<String> projectAdminErrors = Lists.newArrayList();

            for (Project project : allProjects)
            {
                boolean canAdminister = permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, addonUser, false);
                if (!canAdminister)
                {
                    projectAdminErrors.add("Add-on user " + addonUser.getName() + " should have administer permission for project " + project.getKey());
                }
            }

            assertTrue(StringUtils.join(projectAdminErrors, '\n'), projectAdminErrors.isEmpty());
        }
        finally
        {
            if (null != plugin)
            {
                testPluginInstaller.uninstallPlugin(plugin);
            }
        }
    }

    @Test
    public void addonIsMadeAdminOfNewProject() throws Exception
    {
        Plugin plugin = null;
        ApplicationUser admin = null;
        String projectKey = "JEDI";
        try
        {
            plugin = testPluginInstaller.installPlugin(projectAdminAddOn);

            User addonUser = connectAddOnUserService.getUserByAddOnKey(ADDON_KEY);
            admin = userManager.getUserByKey(ADMIN);

            ProjectService.CreateProjectValidationResult result = projectService.validateCreateProject(admin.getDirectoryUser(),
                    "Knights of the Old Republic", projectKey, "It's a trap!", "admin", null, null);
            Project project = projectService.createProject(result);

            boolean addonCanAdministerNewSpace = permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, addonUser, false);
            assertTrue("Add-on user " + addonUser.getName() + " should have administer permission for project " + projectKey, addonCanAdministerNewSpace);
        }
        finally
        {
            if (null != plugin)
            {
                testPluginInstaller.uninstallPlugin(plugin);
            }
            ProjectService.DeleteProjectValidationResult result = projectService.validateDeleteProject(admin.getDirectoryUser(), projectKey);
            projectService.deleteProject(admin, result);
        }
    }
}
