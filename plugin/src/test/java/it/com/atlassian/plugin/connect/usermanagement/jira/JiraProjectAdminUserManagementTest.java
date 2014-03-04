package it.com.atlassian.plugin.connect.usermanagement.jira;

import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddOnUserService;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static com.atlassian.plugin.connect.modules.beans.AuthenticationBean.newAuthenticationBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.LifecycleBean.newLifecycleBean;
import static org.junit.Assert.assertTrue;

@Application ("jira")
@RunWith (AtlassianPluginsTestRunner.class)
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

    private ConnectAddonBean spaceAdminAddon;

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
        this.spaceAdminAddon = newConnectAddonBean()
                .withName("JIRA Project Admin Add-on")
                .withKey(ADDON_KEY)
                .withAuthentication(newAuthenticationBean().withType(AuthenticationType.JWT).build())
                .withLifecycle(
                        newLifecycleBean()
                                .withInstalled(INSTALLED)
                                .build()
                )
                .withScopes(Sets.newHashSet(ScopeName.SPACE_ADMIN))
                .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(ADDON_KEY))
                .build();
    }

    @Test
    public void addonIsMadeAdminOfExistingSpace() throws Exception
    {
        Plugin plugin = null;
        try
        {
            plugin = testPluginInstaller.installPlugin(spaceAdminAddon);

            String addonUserKey = connectAddOnUserService.getOrCreateUserKey(ADDON_KEY);
            ApplicationUser addonUser = userManager.getUserByKey(addonUserKey);

            List<Project> allProjects = projectService.getAllProjects(addonUser).getReturnedValue();

            List<String> spaceAdminErrors = Lists.newArrayList();

            for (Project project : allProjects)
            {
                boolean canAdminister = permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, addonUser, false);
                if (!canAdminister)
                {
                    spaceAdminErrors.add("Add-on user " + addonUserKey + " should have administer permission for project " + project.getKey());
                }
            }

            assertTrue(StringUtils.join(spaceAdminErrors, '\n'), spaceAdminErrors.isEmpty());
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
    public void addonIsMadeAdminOfNewSpace() throws Exception
    {
        Plugin plugin = null;
        Project project = null;
        String projectKey = "JEDI";
        try
        {
            plugin = testPluginInstaller.installPlugin(spaceAdminAddon);

            String addonUserKey = connectAddOnUserService.getOrCreateUserKey(ADDON_KEY);
            ApplicationUser addonUser = userManager.getUserByKey(addonUserKey);

            ApplicationUser admin = userManager.getUserByKey(ADMIN);

//            ProjectService.CreateProjectValidationResult result = new ProjectService.CreateProjectValidationResult(new SimpleErrorCollection(), "Knights of the Old Republic", "JEDI", "It's a trap!", ADMIN, "", 0L, 0L, admin);
//            project = jiraOps.createProject();

//            boolean addonCanAdministerNewSpace = permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, addonUser, false);
            boolean addonCanAdministerNewSpace = false;
            assertTrue("Add-on user " + addonUserKey + " should have administer permission for space " + projectKey, addonCanAdministerNewSpace);
        }
        finally
        {
            if (null != plugin)
            {
                testPluginInstaller.uninstallPlugin(plugin);
            }
//            jiraOps.deleteProject(project);
        }
    }
}
