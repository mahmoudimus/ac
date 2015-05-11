package it.com.atlassian.plugin.connect.jira.scopes;

import com.atlassian.jwt.writer.JwtWriterFactory;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.ConnectAddonRegistry;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.plugin.connect.testsupport.util.auth.TestAuthenticator;
import it.com.atlassian.plugin.connect.plugin.scopes.ScopeTestBase;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * These tests are not exhaustive. They are samples across the cross-product endpoints.
 */
@Application("jira")
@RunWith(AtlassianPluginsTestRunner.class)
public class WriteScopeJiraTest extends ScopeTestBase
{
    public WriteScopeJiraTest(TestPluginInstaller testPluginInstaller,
                              TestAuthenticator testAuthenticator,
                              JwtWriterFactory jwtWriterFactory,
                              ConnectAddonRegistry connectAddonRegistry,
                              ApplicationProperties applicationProperties)
    {
        super(ScopeName.WRITE, testPluginInstaller, testAuthenticator, jwtWriterFactory, connectAddonRegistry, applicationProperties);
    }

    @Test
    public void shouldAllowPutGreenhopperRankBefore() throws Exception
    {
        assertValidRequest(HttpMethod.PUT, "/rest/greenhopper/1.0/api/rank/before");
    }
}
