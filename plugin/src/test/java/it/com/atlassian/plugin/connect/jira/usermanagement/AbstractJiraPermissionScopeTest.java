package it.com.atlassian.plugin.connect.jira.usermanagement;

import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.bc.project.ProjectService.CreateProjectValidationResult;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.compatibility.bridge.project.ProjectCreationData;
import com.atlassian.jira.compatibility.bridge.project.ProjectServiceBridge;
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
import com.atlassian.plugin.connect.util.auth.TestAuthenticator;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.util.List;

import static com.atlassian.plugin.connect.modules.beans.AuthenticationBean.newAuthenticationBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.LifecycleBean.newLifecycleBean;
import static com.atlassian.plugin.connect.util.AddonUtil.randomWebItemBean;
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
    private final ProjectServiceBridge projectServiceBridge;
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
                                                  PermissionManager permissionManager, ProjectService projectService, ProjectServiceBridge projectServiceBridge,
                                                  ProjectRoleService projectRoleService, UserManager userManager,
                                                  TestPluginInstaller testPluginInstaller, TestAuthenticator testAuthenticator)
    {
        this.connectAddOnUserService = connectAddOnUserService;
        this.permissionManager = permissionManager;
        this.projectService = projectService;
        this.projectServiceBridge = projectServiceBridge;
        this.projectRoleService = projectRoleService;
        this.userManager = userManager;
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
    }

    @Before
    public void setUp()
    {
        ConnectAddonBean baseBean = newConnectAddonBean()
                .withKey(ADDON_KEY)
                .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(ADDON_KEY))
                .withName("JIRA Project Admin Add-on")
                .withAuthentication(newAuthenticationBean().withType(AuthenticationType.JWT).build())
                .withLifecycle(
                        newLifecycleBean()
                                .withInstalled(INSTALLED)
                                .build()
                )
                .withModule("webItems", randomWebItemBean())
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

    @After
    public void resetBeans()
    {
        this.adminAddOn = null;

        this.projectAdminAddOn = null;

        this.deleteAddOn = null;

        this.writeAddOn = null;

        this.readAddOn = null;
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
                plugin = testPluginInstaller.installAddon(from);
            }
            plugin = testPluginInstaller.installAddon(to);

            List<String> projectAdminErrors = permissionsForAllProjects(permission, permissionMustExist, plugin);
            assertTrue(StringUtils.join(projectAdminErrors, '\n'), projectAdminErrors.isEmpty());
        }
        finally
        {
            uninstallPlugin(plugin);
        }
    }

    protected List<String> permissionsForAllProjects(Permission permission, boolean permissionMustExist, Plugin plugin) throws ConnectAddOnUserInitException
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
                plugin = testPluginInstaller.installAddon(from);
            }
            plugin = testPluginInstaller.installAddon(to);

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
        return connectAddOnUserService.getOrCreateUserKey(ADDON_KEY, "It's a PROJECT_ADMIN-scoped add-on for tests!");
    }

    protected ApplicationUser getAddOnUser() throws ConnectAddOnUserInitException
    {
        String addonUserName = getAddOnUserName();
        return userManager.getUserByName(addonUserName);
    }

    protected Project createJediProject()
    {
        ApplicationUser admin = userManager.getUserByKey(ADMIN);
        ProjectCreationData projectCreationData = new ProjectCreationData.Builder()
                .withName("Knights of the Old Republic")
                .withKey(PROJECT_KEY)
                .withLead(admin)
                .withDescription("It's a trap!")
                .build();

        CreateProjectValidationResult result = projectServiceBridge.validateCreateProject(admin, projectCreationData);
        return projectService.createProject(result);
    }

    protected void deleteJediProject()
    {
        ApplicationUser admin = userManager.getUserByKey(ADMIN);
        ProjectService.DeleteProjectValidationResult result = projectService.validateDeleteProject(admin, PROJECT_KEY);

        if(result.isValid())
        {
            projectService.deleteProject(admin, result);
        }
    }

    protected void uninstallPlugin(Plugin plugin) throws IOException
    {
        if (null != plugin)
        {
            testPluginInstaller.uninstallAddon(plugin);
        }
    }

    protected Plugin installPlugin(ConnectAddonBean addon) throws IOException
    {
        return testPluginInstaller.installAddon(addon);
    }

    protected void disablePlugin(Plugin plugin) throws IOException
    {
        testPluginInstaller.disableAddon(plugin.getKey());
    }

    protected void enablePlugin(Plugin plugin) throws IOException
    {
        testPluginInstaller.enableAddon(plugin.getKey());
    }
}
