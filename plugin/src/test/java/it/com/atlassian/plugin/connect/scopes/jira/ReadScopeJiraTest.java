package it.com.atlassian.plugin.connect.scopes.jira;

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
import org.junit.Test;
import org.junit.runner.RunWith;

@Application("jira")
@RunWith(AtlassianPluginsTestRunner.class)
public class ReadScopeJiraTest extends ScopeTestBase
{

    public ReadScopeJiraTest(TestPluginInstaller testPluginInstaller,
                             TestAuthenticator testAuthenticator,
                             JwtWriterFactory jwtWriterFactory,
                             ConnectAddonRegistry connectAddonRegistry,
                             ApplicationProperties applicationProperties)
    {
        super(ScopeName.READ, testPluginInstaller, testAuthenticator, jwtWriterFactory, connectAddonRegistry,
                applicationProperties);
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
}
