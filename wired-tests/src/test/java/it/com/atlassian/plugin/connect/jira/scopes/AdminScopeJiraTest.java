package it.com.atlassian.plugin.connect.jira.scopes;

import com.atlassian.jwt.writer.JwtWriterFactory;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.ConnectAddonRegistry;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.util.auth.TestAuthenticator;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.sal.api.ApplicationProperties;
import it.com.atlassian.plugin.connect.plugin.scopes.ScopeTestBase;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

@Application ("jira")
@RunWith (AtlassianPluginsTestRunner.class)
public class AdminScopeJiraTest extends ScopeTestBase
{
    public AdminScopeJiraTest(TestPluginInstaller testPluginInstaller,
            TestAuthenticator testAuthenticator,
            JwtWriterFactory jwtWriterFactory,
            ConnectAddonRegistry connectAddonRegistry,
            ApplicationProperties applicationProperties)
    {
        super(ScopeName.ADMIN, testPluginInstaller, testAuthenticator, jwtWriterFactory, connectAddonRegistry, applicationProperties);
    }

    @Test
    public void shouldAllowUsageOfRoleResource() throws IOException, NoSuchAlgorithmException
    {
        assertValidRequest(HttpMethod.GET, "/rest/api/2/role");
    }

    @Test
    public void shouldAllowUsageOfRoleResourceForSingleRole() throws IOException, NoSuchAlgorithmException
    {
        assertValidRequest(HttpMethod.GET, "/rest/api/2/role/10000");
    }
}
