package it.com.atlassian.plugin.connect.usermanagement.jira;

import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.permission.Permission;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserInitException;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserService;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.com.atlassian.plugin.connect.TestAuthenticator;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;

import java.io.IOException;
import java.util.List;

import static com.atlassian.plugin.connect.modules.beans.AuthenticationBean.newAuthenticationBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.LifecycleBean.newLifecycleBean;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public abstract class AbstractJiraPermissionScopeTest
{
    private static final String ADMIN = "admin";
    private static final String PROJECT_KEY = "JEDI";
    private static String ADDON_KEY = "project-admin-ADDON"; // Use uppercase characters to detect username vs userkey issues
    private static final String INSTALLED = "/installed";

    private final ConnectAddOnUserService connectAddOnUserService;
    private final PermissionManager permissionManager;
    private final ProjectService projectService;
    private final ProjectRoleService projectRoleService;
    private final UserManager userManager;
    private final TestPluginInstaller testPluginInstaller;
    private final TestAuthenticator testAuthenticator;

    private ConnectAddonBean adminAddOn;
    private ConnectAddonBean projectAdminAddOn;
    private ConnectAddonBean deleteAddOn;
    private ConnectAddonBean writeAddOn;
    private ConnectAddonBean readAddOn;

    public AbstractJiraPermissionScopeTest(ConnectAddOnUserService connectAddOnUserService,
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

        this.adminAddOn = newConnectAddonBean(baseBean)
                .withScopes(Sets.newHashSet(ScopeName.ADMIN))
                .build();
        this.projectAdminAddOn = newConnectAddonBean(baseBean)
                .withScopes(Sets.newHashSet(ScopeName.PROJECT_ADMIN))
                .build();
        this.deleteAddOn = newConnectAddonBean(baseBean)
                .withScopes(Sets.newHashSet(ScopeName.DELETE))
                .build();
        this.writeAddOn = newConnectAddonBean(baseBean)
                .withScopes(Sets.newHashSet(ScopeName.WRITE))
                .build();
        this.readAddOn = newConnectAddonBean(baseBean)
                .withScopes(Sets.newHashSet(ScopeName.READ))
                .build();

        testAuthenticator.authenticateUser("admin");
    }

    public ConnectAddonBean getAdminAddOn()
    {
        return adminAddOn;
    }

    public ConnectAddonBean getProjectAdminAddOn()
    {
        return projectAdminAddOn;
    }

    public ConnectAddonBean getDeleteAddOn()
    {
        return deleteAddOn;
    }

    public ConnectAddonBean getWriteAddOn()
    {
        return writeAddOn;
    }

    public ConnectAddonBean getReadAddOn()
    {
        return readAddOn;
    }

    public ConnectAddOnUserService getConnectAddOnUserService()
    {
        return connectAddOnUserService;
    }

    public PermissionManager getPermissionManager()
    {
        return permissionManager;
    }

    public ProjectService getProjectService()
    {
        return projectService;
    }

    public ProjectRoleService getProjectRoleService()
    {
        return projectRoleService;
    }

    public UserManager getUserManager()
    {
        return userManager;
    }

    public TestPluginInstaller getTestPluginInstaller()
    {
        return testPluginInstaller;
    }

    public TestAuthenticator getTestAuthenticator()
    {
        return testAuthenticator;
    }

    protected void checkHasPermissionForAllProjectsAfterInstall(ConnectAddonBean addon, Permission permission) throws Exception
    {
        checkPermissionsForAllProjectsAfterTransition(null, addon, permission, true);
    }

    protected void checkHasNoPermissionForAnyProjectAfterInstall(ConnectAddonBean addon, Permission permission) throws Exception
    {
        checkPermissionsForAllProjectsAfterTransition(null, addon, permission, false);
    }

    protected void checkHasPermissionForNewProjectAfterInstall(ConnectAddonBean addon, Permission permission) throws Exception
    {
        checkPermissionForNewProject(null, addon, permission, true);
    }

    protected void checkHasNoPermissionForNewProjectAfterInstall(ConnectAddonBean addon, Permission permission) throws Exception
    {
        checkPermissionForNewProject(null, addon, permission, false);
    }

    protected void checkHasPermissionForAllProjectsAfterTransition(ConnectAddonBean from, ConnectAddonBean to, Permission permission) throws Exception
    {
        checkPermissionsForAllProjectsAfterTransition(from, to, permission, true);
    }

    protected void checkHasNoPermissionForAnyProjectAfterTransition(ConnectAddonBean from, ConnectAddonBean to, Permission permission) throws Exception
    {
        checkPermissionsForAllProjectsAfterTransition(from, to, permission, false);
    }

    protected void checkHasPermissionForNewProjectAfterTransition(ConnectAddonBean from, ConnectAddonBean to, Permission permission) throws Exception
    {
        checkPermissionForNewProject(from, to, permission, true);
    }

    protected void checkHasNoPermissionForNewProjectAfterTransition(ConnectAddonBean from, ConnectAddonBean to, Permission permission) throws Exception
    {
        checkPermissionForNewProject(from, to, permission, false);
    }

    private void checkPermissionsForAllProjectsAfterTransition(ConnectAddonBean from, ConnectAddonBean to, Permission permission, boolean permissionMustExist) throws Exception
    {
        Plugin plugin = null;
        try
        {
            if (from != null)
            {
                plugin = testPluginInstaller.installPlugin(from);
            }
            plugin = testPluginInstaller.installPlugin(to);

            List<String> projectAdminErrors = permissionsForAllProjects(permission, permissionMustExist);
            assertTrue(StringUtils.join(projectAdminErrors, '\n'), projectAdminErrors.isEmpty());
        }
        finally
        {
            uninstallPlugin(plugin);
        }
    }

    protected List<String> permissionsForAllProjects(Permission permission, boolean permissionMustExist) throws ConnectAddOnUserInitException
    {
        ApplicationUser addonUser = getAddOnUser();
        List<Project> allProjects = projectService.getAllProjects(addonUser).getReturnedValue();
        List<String> projectAdminErrors = Lists.newArrayList();

        for (Project project : allProjects)
        {
            boolean hasPermission = permissionManager.hasPermission(permission.getId(), project, addonUser, false);
            if (!hasPermission && permissionMustExist)
            {
                projectAdminErrors.add("Add-on user " + addonUser.getKey() + " should have '"+ permission +"' permission for project " + project.getKey());
            }
            else if (hasPermission && !permissionMustExist)
            {
                projectAdminErrors.add("Add-on user " + addonUser.getKey() + " should not have '"+ permission +"' permission for project " + project.getKey());
            }
        }
        return projectAdminErrors;
    }

    private void checkPermissionForNewProject(ConnectAddonBean from, ConnectAddonBean to, Permission permission, boolean permissionMustExist) throws Exception
    {
        Plugin plugin = null;
        try
        {
            if (from != null)
            {
                plugin = testPluginInstaller.installPlugin(from);
            }
            plugin = testPluginInstaller.installPlugin(to);

            Project project = createJediProject();
            ApplicationUser addonUser = getAddOnUser();

            boolean hasPermission = permissionManager.hasPermission(permission.getId(), project, addonUser, false);

            if (permissionMustExist)
            {
                assertTrue("Add-on user " + addonUser.getName() + " should have '" + permission + "' permission for project " + PROJECT_KEY, hasPermission);
            }
            else
            {
                assertFalse("Add-on user " + addonUser.getName() + " should not have '"+ permission +"'  permission for project " + PROJECT_KEY, hasPermission);
            }
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

    protected ApplicationUser getAddOnUser() throws ConnectAddOnUserInitException
    {
        String addonUserName = getAddOnUserName();
        return userManager.getUserByName(addonUserName);
    }

    protected Project createJediProject()
    {
        ApplicationUser admin = userManager.getUserByKey(ADMIN);
        ProjectService.CreateProjectValidationResult result = projectService.validateCreateProject(admin.getDirectoryUser(),
                "Knights of the Old Republic", PROJECT_KEY, "It's a trap!", "admin", null, null);
        return projectService.createProject(result);
    }

    protected void deleteJediProject()
    {
        ApplicationUser admin = userManager.getUserByKey(ADMIN);
        ProjectService.DeleteProjectValidationResult result = projectService.validateDeleteProject(admin.getDirectoryUser(), PROJECT_KEY);
        projectService.deleteProject(admin, result);
    }

    protected void uninstallPlugin(Plugin plugin) throws IOException
    {
        if (null != plugin)
        {
            testPluginInstaller.uninstallPlugin(plugin);
        }
    }

    protected Plugin installPlugin(ConnectAddonBean addon) throws IOException
    {
        return testPluginInstaller.installPlugin(addon);
    }

    protected void disablePlugin(Plugin plugin) throws IOException
    {
        testPluginInstaller.disablePlugin(plugin.getKey());
    }

    protected void enablePlugin(Plugin plugin) throws IOException
    {
        testPluginInstaller.enablePlugin(plugin.getKey());
    }
}
