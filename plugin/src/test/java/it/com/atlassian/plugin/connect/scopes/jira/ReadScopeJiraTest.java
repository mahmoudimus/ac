
package it.com.atlassian.plugin.connect.scopes.jira;

import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jwt.writer.JwtWriterFactory;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.sal.api.ApplicationProperties;
import it.com.atlassian.plugin.connect.TestAuthenticator;
import it.com.atlassian.plugin.connect.scopes.ScopeTestBase;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

@Application("jira")
@RunWith(AtlassianPluginsTestRunner.class)
public class ReadScopeJiraTest extends ScopeTestBase
{
    private static final String ADMIN_USERNAME = "admin";

    private final UserManager userManager;
    private final ProjectService projectService;

    public ReadScopeJiraTest(TestPluginInstaller testPluginInstaller,
                             TestAuthenticator testAuthenticator,
                             JwtWriterFactory jwtWriterFactory,
                             ConnectAddonRegistry connectAddonRegistry,
                             ApplicationProperties applicationProperties,
                             UserManager userManager,
                             ProjectService projectService)
    {
        super(ScopeName.READ, testPluginInstaller, testAuthenticator, jwtWriterFactory, connectAddonRegistry,
                applicationProperties);
        this.userManager = userManager;
        this.projectService = projectService;
    }

    @Test
    public void shouldAllowGetGreenhopperRapidview() throws Exception
    {
        assertValidRequest(HttpMethod.GET, "/rest/greenhopper/1.0/rapidview");
    }

    @Test
    public void shouldForbidPutGreenhopperRankBefore() throws Exception
    {
        assertForbiddenRequest(HttpMethod.PUT, "/rest/greenhopper/1.0/api/rank/before");
    }

    @Test
    public void shouldAllowGetSecureProjectAvatar() throws Exception
    {
        Project project = createProject();

        assertValidRequest(HttpMethod.GET, "/secure/projectavatar?pid=" + project.getId());
    }

    @Test
    public void shouldAllowGetSecureUserAvatar() throws Exception
    {
        assertValidRequest(HttpMethod.GET, "/secure/useravatar?ownerId=" + ADMIN_USERNAME);
    }

    private Project createProject() throws IOException
    {
        int keyLength = 6;
        String key = RandomStringUtils.randomAlphabetic(keyLength).toUpperCase();
        ApplicationUser user = userManager.getUserByKey(ADMIN_USERNAME);
        ProjectService.CreateProjectValidationResult result = projectService.validateCreateProject(
                user.getDirectoryUser(), key, key, null, ADMIN_USERNAME, null, null);
        return projectService.createProject(result);
    }
}
