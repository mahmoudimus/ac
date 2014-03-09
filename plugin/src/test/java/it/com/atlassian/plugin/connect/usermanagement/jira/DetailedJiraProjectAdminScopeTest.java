package it.com.atlassian.plugin.connect.usermanagement.jira;

import com.atlassian.jira.bc.ServiceResult;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.actor.UserRoleActorFactory;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserInitException;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserService;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.com.atlassian.plugin.connect.TestAuthenticator;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.atlassian.plugin.connect.modules.beans.AuthenticationBean.newAuthenticationBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.LifecycleBean.newLifecycleBean;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Application("jira")
@RunWith(AtlassianPluginsTestRunner.class)
public class DetailedJiraProjectAdminScopeTest
{
    private static final String ADMIN = "admin";
    private static final String PROJECT_KEY = "JEDI";
    private static String ADDON_KEY = "project-admin-ADDON"; // Use uppercase character to detect username vs userkey issues
    private static final String INSTALLED = "/installed";

    private final ConnectAddOnUserService connectAddOnUserService;
    private final PermissionManager permissionManager;
    private final ProjectService projectService;
    private final ProjectRoleService projectRoleService;
    private final UserManager userManager;
    private final TestPluginInstaller testPluginInstaller;
    private final TestAuthenticator testAuthenticator;

    private ConnectAddonBean projectAdminAddOn;
    private ConnectAddonBean writeAddOn;
    private ConnectAddonBean adminAddOn;

    public DetailedJiraProjectAdminScopeTest(ConnectAddOnUserService connectAddOnUserService,
                                             PermissionManager permissionManager, ProjectService projectService,
                                             ProjectRoleService projectRoleService, UserManager userManager,
                                             TestPluginInstaller testPluginInstaller, TestAuthenticator testAuthenticator)
    {
        this.connectAddOnUserService = connectAddOnUserService;
        this.permissionManager = permissionManager;
        this.projectService = projectService;
        this.projectRoleService = projectRoleService;
        this.userManager = userManager;
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
    }

    @Before
    public void setUp()
    {
        ConnectAddonBean baseBean = newConnectAddonBean()
                .withName("JIRA Project Admin Add-on")
                .withKey(ADDON_KEY)
                .withAuthentication(newAuthenticationBean().withType(AuthenticationType.JWT).build())
                .withLifecycle(
                        newLifecycleBean()
                                .withInstalled(INSTALLED)
                                .build()
                )
                .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(ADDON_KEY))
                .build();

        this.projectAdminAddOn = newConnectAddonBean(baseBean)
                .withScopes(Sets.newHashSet(ScopeName.PROJECT_ADMIN))
                .build();

        this.writeAddOn = newConnectAddonBean(baseBean)
                .withScopes(Sets.newHashSet(ScopeName.WRITE))
                .build();

        this.adminAddOn = newConnectAddonBean(baseBean)
                .withScopes(Sets.newHashSet(ScopeName.ADMIN))
                .build();

        //you MUST login as admin before you can use the testPluginInstaler
        testAuthenticator.authenticateUser("admin");
    }

    @Test
    public void addonIsMadeAdminOfExistingProjects() throws Exception
    {
        Plugin plugin = null;
        try
        {
            plugin = testPluginInstaller.installPlugin(projectAdminAddOn);
            List<String> projectAdminErrors = addOnUserIsProjectAdminForAllProjects();
            assertTrue(StringUtils.join(projectAdminErrors, '\n'), projectAdminErrors.isEmpty());
        }
        finally
        {
            uninstallPlugin(plugin);
        }
    }

    @Test
    public void addonIsMadeAdminOfNewProject() throws Exception
    {
        Plugin plugin = null;
        try
        {
            plugin = testPluginInstaller.installPlugin(projectAdminAddOn);

            Project project = createJediProject();
            ApplicationUser addonUser = getAddOnUser();

            boolean addonCanAdministerNewProject = permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, addonUser, false);
            assertTrue("Add-on user " + addonUser.getName() + " should have administer permission for project " + PROJECT_KEY, addonCanAdministerNewProject);
        }
        finally
        {
            uninstallPlugin(plugin);
            deleteJediProject();
        }
    }

    @Test
    public void addOnIsNoLongerAdminOfExistingProjectsAfterScopeContraction() throws Exception
    {
        Plugin plugin = null;
        try
        {
            plugin = testPluginInstaller.installPlugin(projectAdminAddOn);
            plugin = testPluginInstaller.installPlugin(writeAddOn);

            List<String> projectAdminErrors = addOnUserIsNotProjectAdminForAnyProjects();
            assertTrue(StringUtils.join(projectAdminErrors, '\n'), projectAdminErrors.isEmpty());
        }
        finally
        {
            uninstallPlugin(plugin);
        }
    }

    @Test
    public void addOnIsNoLongerAdminOfNewProjectsAfterScopeContraction() throws Exception
    {
        Plugin plugin = null;
        try
        {
            plugin = testPluginInstaller.installPlugin(projectAdminAddOn);
            plugin = testPluginInstaller.installPlugin(writeAddOn);

            Project project = createJediProject();
            ApplicationUser addonUser = getAddOnUser();

            boolean addonCanAdministerNewProject = permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, addonUser, false);
            assertFalse("Add-on user " + addonUser.getName() + " should not have administer permission for project " + PROJECT_KEY, addonCanAdministerNewProject);
        }
        finally
        {
            uninstallPlugin(plugin);
            deleteJediProject();
        }
    }

    @Test
    public void addOnCanAdministerExistingProjectsAfterScopeExpansion() throws Exception
    {
        Plugin plugin = null;
        try
        {
            plugin = testPluginInstaller.installPlugin(projectAdminAddOn);
            plugin = testPluginInstaller.installPlugin(adminAddOn);

            List<String> projectAdminErrors = addOnUserCanUpdateAllProjects();
            assertTrue(StringUtils.join(projectAdminErrors, '\n'), projectAdminErrors.isEmpty());
        }
        finally
        {
            uninstallPlugin(plugin);
        }
    }

    @Test
    public void addOnCanAdministerNewProjectsAfterScopeExpansion() throws Exception
    {
        Plugin plugin = null;
        try
        {
            plugin = testPluginInstaller.installPlugin(projectAdminAddOn);
            plugin = testPluginInstaller.installPlugin(adminAddOn);

            Project project = createJediProject();
            ApplicationUser addonUser = getAddOnUser();

            ServiceResult canUpdateProjectResult = projectService.validateUpdateProject(addonUser, project.getKey());
            assertTrue("Add-on user " + addonUser.getName() + " should be able to update project " + PROJECT_KEY, canUpdateProjectResult.isValid());
        }
        finally
        {
            uninstallPlugin(plugin);
            deleteJediProject();
        }
    }

    @Test
    public void projectAdminConfigurationIsRemovedAfterScopeExpansion() throws Exception
    {
        Plugin plugin = null;
        try
        {
            plugin = testPluginInstaller.installPlugin(projectAdminAddOn);
            plugin = testPluginInstaller.installPlugin(adminAddOn);

            // this is checking explicitly for the project_admin permission, which should have been removed
            List<String> projectAdminErrors = addOnUserIsNotProjectAdminForAnyProjects();
            assertTrue(StringUtils.join(projectAdminErrors, '\n'), projectAdminErrors.isEmpty());
        }
        finally
        {
            uninstallPlugin(plugin);
        }
    }

    @Test
    public void projectAdminConfigurationIsRemovedAfterReinstallWithDowngradedScope() throws Exception
    {
        Plugin plugin = null;
        try
        {
            plugin = testPluginInstaller.installPlugin(projectAdminAddOn);
            testPluginInstaller.uninstallPlugin(plugin);
            plugin = testPluginInstaller.installPlugin(writeAddOn);

            List<String> projectAdminErrors = addOnUserIsNotProjectAdminForAnyProjects();
            assertTrue(StringUtils.join(projectAdminErrors, '\n'), projectAdminErrors.isEmpty());
        }
        finally
        {
            uninstallPlugin(plugin);
        }
    }

    @Test
    public void addOnEnablementChangesPreserveCustomConfiguration() throws Exception
    {
        Plugin plugin = null;
        try
        {
            plugin = testPluginInstaller.installPlugin(projectAdminAddOn);

            Project project = createJediProject();
            ApplicationUser addonUser = getAddOnUser();

            SimpleErrorCollection errorCollection = new SimpleErrorCollection();
            ProjectRole projectRole = projectRoleService.getProjectRoleByName("atlassian-addons-project-admin", errorCollection);
            projectRoleService.removeActorsFromProjectRole(
                    Collections.singleton(addonUser.getKey()),
                    projectRole,
                    project,
                    UserRoleActorFactory.TYPE,
                    errorCollection);

            boolean addonCannotAdministerProject = permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, addonUser, false);
            assertFalse("Add-on user " + addonUser.getName() + " should not have administer permission for project " + PROJECT_KEY, addonCannotAdministerProject);

            testPluginInstaller.disablePlugin(ADDON_KEY);
            testPluginInstaller.enablePlugin(ADDON_KEY);

            boolean addonStillCannotAdministerProject = permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, addonUser, false);
            assertFalse("Add-on user " + addonUser.getName() + " should not have administer permission for project " + PROJECT_KEY, addonStillCannotAdministerProject);
        }
        finally
        {
            uninstallPlugin(plugin);
            deleteJediProject();
        }
    }

    private String getAddOnUserName() throws ConnectAddOnUserInitException
    {
        return connectAddOnUserService.getOrCreateUserKey(ADDON_KEY);
    }

    private ApplicationUser getAddOnUser() throws ConnectAddOnUserInitException
    {
        String addonUserName = getAddOnUserName();
        return userManager.getUserByName(addonUserName);
    }

    private List<String> addOnUserCanUpdateAllProjects() throws ConnectAddOnUserInitException
    {
        ApplicationUser addonUser = getAddOnUser();
        List<Project> allProjects = projectService.getAllProjects(addonUser).getReturnedValue();
        List<String> projectAdminErrors = Lists.newArrayList();

        for (Project project : allProjects)
        {
            ServiceResult canUpdateProjectResult = projectService.validateUpdateProject(addonUser, project.getKey());
            if (!canUpdateProjectResult.isValid())
            {
                projectAdminErrors.add("Add-on user " + addonUser.getKey() + " should be able to update project " + project.getKey());
            }
        }
        return projectAdminErrors;
    }

    private List<String> addOnUserIsProjectAdminForAllProjects() throws ConnectAddOnUserInitException
    {
        ApplicationUser addonUser = getAddOnUser();
        List<Project> allProjects = projectService.getAllProjects(addonUser).getReturnedValue();
        List<String> projectAdminErrors = Lists.newArrayList();

        for (Project project : allProjects)
        {
            boolean canAdminister = permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, addonUser, false);
            if (!canAdminister)
            {
                projectAdminErrors.add("Add-on user " + addonUser.getKey() + " should have administer permission for project " + project.getKey());
            }
        }
        return projectAdminErrors;
    }

    private List<String> addOnUserIsNotProjectAdminForAnyProjects() throws ConnectAddOnUserInitException
    {
        ApplicationUser addonUser = getAddOnUser();
        List<Project> allProjects = projectService.getAllProjects(addonUser).getReturnedValue();
        List<String> projectAdminErrors = Lists.newArrayList();

        for (Project project : allProjects)
        {
            boolean canAdminister = permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, addonUser, false);
            if (canAdminister)
            {
                projectAdminErrors.add("Add-on user " + addonUser.getKey() + " should not have administer permission for project " + project.getKey());
            }
        }
        return projectAdminErrors;
    }

    private Project createJediProject()
    {
        ApplicationUser admin = userManager.getUserByKey(ADMIN);
        ProjectService.CreateProjectValidationResult result = projectService.validateCreateProject(admin.getDirectoryUser(),
                "Knights of the Old Republic", PROJECT_KEY, "It's a trap!", "admin", null, null);
        return projectService.createProject(result);
    }

    private void deleteJediProject()
    {
        ApplicationUser admin = userManager.getUserByKey(ADMIN);
        ProjectService.DeleteProjectValidationResult result = projectService.validateDeleteProject(admin.getDirectoryUser(), PROJECT_KEY);
        projectService.deleteProject(admin, result);
    }

    private void uninstallPlugin(Plugin plugin) throws IOException
    {
        if (null != plugin)
        {
            testPluginInstaller.uninstallPlugin(plugin);
        }
    }
}
