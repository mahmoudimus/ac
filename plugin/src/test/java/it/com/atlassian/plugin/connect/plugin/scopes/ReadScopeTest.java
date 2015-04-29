package it.com.atlassian.plugin.connect.plugin.scopes;

import com.atlassian.jwt.writer.JwtWriterFactory;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.ConnectAddonRegistry;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.plugin.connect.util.auth.TestAuthenticator;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * These tests are not exhaustive. They are samples across the cross-product endpoints.
 */
@RunWith(AtlassianPluginsTestRunner.class)
public class ReadScopeTest extends ScopeTestBase
{
    public ReadScopeTest(TestPluginInstaller testPluginInstaller,
                         TestAuthenticator testAuthenticator,
                         JwtWriterFactory jwtWriterFactory,
                         ConnectAddonRegistry connectAddonRegistry,
                         ApplicationProperties applicationProperties)
    {
        super(ScopeName.READ, testPluginInstaller, testAuthenticator, jwtWriterFactory, connectAddonRegistry,
                applicationProperties);
    }

    @Test
    public void shouldAllowGetApplinksEntities() throws Exception
    {
        assertValidRequest(HttpMethod.GET, "/rest/applinks/2.0/entities");
    }

    @Test
    public void shouldForbidPostApplinksEntities() throws Exception
    {
        assertForbiddenRequest(HttpMethod.POST, "/rest/applinks/2.0/entities");
    }
}
