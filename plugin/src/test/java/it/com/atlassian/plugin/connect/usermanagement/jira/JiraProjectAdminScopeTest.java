package it.com.atlassian.plugin.connect.usermanagement.jira;

import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jwt.applinks.JwtApplinkFinder;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertTrue;

@Application ("jira")
@RunWith (AtlassianPluginsTestRunner.class)
public class JiraProjectAdminScopeTest extends JiraAdminScopeTestBase
{
    private final ProjectService projectService;

    public JiraProjectAdminScopeTest(TestPluginInstaller testPluginInstaller, JwtApplinkFinder jwtApplinkFinder,
            PermissionManager jiraPermissionManager, ProjectService projectService, UserManager userManager)
    {
        super(testPluginInstaller, jwtApplinkFinder, jiraPermissionManager, userManager);
        this.projectService = projectService;
    }

    @Override
    protected ScopeName getScope()
    {
        return ScopeName.SPACE_ADMIN;
    }

    @Override
    protected boolean shouldBeAdmin()
    {
        return false;
    }

    @Test
    public void addonIsMadeAdminOfExistingProjects() throws Exception
    {
        ApplicationUser addonUser = getAddonUser();

        List<Project> allProjects = projectService.getAllProjects(addonUser).getReturnedValue();

        List<String> projectAdminErrors = Lists.newArrayList();

        for (Project project : allProjects)
        {
            boolean canAdminister = jiraPermissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, addonUser, false);
            if (!canAdminister)
            {
                projectAdminErrors.add("Add-on user " + getAddonUserKey() + " should have administer permission for project " + project.getKey());
            }
        }

        assertTrue(StringUtils.join(projectAdminErrors, '\n'), projectAdminErrors.isEmpty());
    }

    @Test
    public void addonIsMadeAdminOfNewProject() throws Exception
    {
        ApplicationUser addonUser = getAddonUser();

        ApplicationUser admin = userManager.getUserByKey("admin");

        String projectKey = "JEDI";

//            ProjectService.CreateProjectValidationResult result = new ProjectService.CreateProjectValidationResult(new SimpleErrorCollection(), "Knights of the Old Republic", projectKey, "It's a trap!", ADMIN, "", 0L, 0L, admin);
//            project = jiraOps.createProject();

//            boolean addonCanAdministerNewProject = permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, addonUser, false);
        boolean addonCanAdministerNewProject = false;
        assertTrue("Add-on user " + getAddonUserKey() + " should have administer permission for project " + projectKey, addonCanAdministerNewProject);
    }
}
