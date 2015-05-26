package it.com.atlassian.plugin.connect.plugin.scopes;

import com.atlassian.jwt.writer.JwtWriterFactory;
import com.atlassian.plugin.connect.api.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.plugin.connect.testsupport.util.auth.TestAuthenticator;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * These tests are not exhaustive. They are samples across the cross-product endpoints.
 */
@RunWith(AtlassianPluginsTestRunner.class)
public class NoScopeTest extends ScopeTestBase
{
    public NoScopeTest(TestPluginInstaller testPluginInstaller,
                       TestAuthenticator testAuthenticator,
                       JwtWriterFactory jwtWriterFactory,
                       ConnectAddonRegistry connectAddonRegistry,
                       ApplicationProperties applicationProperties)
    {
        super(null, testPluginInstaller, testAuthenticator, jwtWriterFactory, connectAddonRegistry, applicationProperties);
    }

    @Test
    public void shouldForbidGetAtlassianConnectLicense() throws Exception
    {
        assertForbiddenRequest(HttpMethod.GET, "/rest/atlassian-connect/1/license");
    }

    @Test
    public void shouldForbidGetPluginsServletAtlassianConnect() throws Exception
    {
        assertForbiddenRequest(HttpMethod.GET, "/plugins/servlet/atlassian-connect");
    }

    @Test
    public void shouldForbidGetApplinksEntities() throws Exception
    {
        assertForbiddenRequest(HttpMethod.GET, "/rest/applinks/2.0/entities");
    }

    @Test
    public void shouldForbidGetGreenhopperRapidview() throws Exception
    {
        assertForbiddenRequest(HttpMethod.GET, "/rest/greenhopper/1.0/rapidview");
    }

    @Test
    public void shouldForbidPutGreenhopperRankBefore() throws Exception
    {
        assertForbiddenRequest(HttpMethod.PUT, "/rest/greenhopper/1.0/api/rank/before");
    }
}
