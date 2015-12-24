package it.com.atlassian.plugin.connect.jira.auth;

import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.permission.Permission;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.lifecycle.ConnectAddonInitException;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.spi.auth.user.ConnectUserService;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.util.auth.TestAuthenticator;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.com.atlassian.plugin.connect.jira.util.JiraTestUtil;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
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

    private static final String PROJECT_KEY = "JEDI";
    private static String ADDON_KEY = "project-admin-ADDON"; // Use uppercase characters to detect username vs userkey issues
    private static final String INSTALLED = "/installed";

    private final ConnectUserService connectUserService;
    private final PermissionManager permissionManager;
    private final ProjectService projectService;
    private final ProjectRoleService projectRoleService;
    private final UserManager userManager;
    private final TestPluginInstaller testPluginInstaller;
    private final TestAuthenticator testAuthenticator;
    protected final JiraTestUtil jiraTestUtil;

    private ConnectAddonBean adminAddon;
    private ConnectAddonBean projectAdminAddon;
    private ConnectAddonBean deleteAddon;
    private ConnectAddonBean writeAddon;
    private ConnectAddonBean readAddon;

    public AbstractJiraPermissionScopeTest(ConnectUserService connectUserService,
                                           PermissionManager permissionManager,
                                           ProjectService projectService,
                                           ProjectRoleService projectRoleService,
                                           UserManager userManager,
                                           TestPluginInstaller testPluginInstaller,
                                           TestAuthenticator testAuthenticator,
                                           JiraTestUtil jiraTestUtil)
    {
        this.connectUserService = connectUserService;
        this.permissionManager = permissionManager;
        this.projectService = projectService;
        this.projectRoleService = projectRoleService;
        this.userManager = userManager;
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
        this.jiraTestUtil = jiraTestUtil;
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
                .build();

        this.adminAddon = newConnectAddonBean(baseBean)
                .withScopes(Sets.newHashSet(ScopeName.ADMIN))
                .build();

        this.projectAdminAddon = newConnectAddonBean(baseBean)
                .withScopes(Sets.newHashSet(ScopeName.PROJECT_ADMIN))
                .build();

        this.deleteAddon = newConnectAddonBean(baseBean)
                .withScopes(Sets.newHashSet(ScopeName.DELETE))
                .build();

        this.writeAddon = newConnectAddonBean(baseBean)
                .withScopes(Sets.newHashSet(ScopeName.WRITE))
                .build();

        this.readAddon = newConnectAddonBean(baseBean)
                .withScopes(Sets.newHashSet(ScopeName.READ))
                .build();

        testAuthenticator.authenticateUser("admin");
    }

    @After
    public void resetBeans()
    {
        this.adminAddon = null;

        this.projectAdminAddon = null;

        this.deleteAddon = null;

        this.writeAddon = null;

        this.readAddon = null;
    }

    public ConnectAddonBean getAdminAddon()
    {
        return adminAddon;
    }

    public ConnectAddonBean getProjectAdminAddon()
    {
        return projectAdminAddon;
    }

    public ConnectAddonBean getDeleteAddon()
    {
        return deleteAddon;
    }

    public ConnectAddonBean getWriteAddon()
    {
        return writeAddon;
    }

    public ConnectAddonBean getReadAddon()
    {
        return readAddon;
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

    protected List<String> permissionsForAllProjects(Permission permission, boolean permissionMustExist, Plugin plugin) throws ConnectAddonInitException
    {
        ApplicationUser addonUser = getAddonUser();
        List<Project> allProjects = projectService.getAllProjects(addonUser).getReturnedValue();
        List<String> projectAdminErrors = Lists.newArrayList();

        for (Project project : allProjects)
        {
            boolean hasPermission = permissionManager.hasPermission(permission.getId(), project, addonUser, false);
            if (!hasPermission && permissionMustExist)
            {
                projectAdminErrors.add("Add-on user " + addonUser.getKey() + " should have '" + permission + "' permission for project " + project.getKey());
            }
            else if (hasPermission && !permissionMustExist)
            {
                projectAdminErrors.add("Add-on user " + addonUser.getKey() + " should not have '" + permission + "' permission for project " + project.getKey());
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

            Project project = jiraTestUtil.createProject();
            ApplicationUser addonUser = getAddonUser();

            boolean hasPermission = permissionManager.hasPermission(permission.getId(), project, addonUser, false);

            if (permissionMustExist)
            {
                assertTrue("Add-on user " + addonUser.getName() + " should have '" + permission + "' permission for project " + PROJECT_KEY, hasPermission);
            }
            else
            {
                assertFalse("Add-on user " + addonUser.getName() + " should not have '" + permission + "'  permission for project " + PROJECT_KEY, hasPermission);
            }
        }
        finally
        {
            uninstallPlugin(plugin);
        }
    }

    private String getAddonUserName() throws ConnectAddonInitException
    {
        return connectUserService.getOrCreateAddonUserName(ADDON_KEY, "It's a PROJECT_ADMIN-scoped add-on for tests!");
    }

    protected ApplicationUser getAddonUser() throws ConnectAddonInitException
    {
        String addonUserName = getAddonUserName();
        return userManager.getUserByName(addonUserName);
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
