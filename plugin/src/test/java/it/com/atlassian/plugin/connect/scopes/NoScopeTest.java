package it.com.atlassian.plugin.connect.scopes;

import com.atlassian.jwt.writer.JwtWriterFactory;
import com.atlassian.plugin.connect.plugin.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.sal.api.ApplicationProperties;
import it.com.atlassian.plugin.connect.ParameterizedWiredTest;
import it.com.atlassian.plugin.connect.TestAuthenticator;

@ParameterizedWiredTest
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

    /**
     * These tests are not exhaustive. They are samples across the cross-product endpoints.
     */
    @ParameterizedWiredTest.Parameters(name="httpRequest", length=3)
    protected Object[][] data = new Object[][]
    {
            new Object[]{ HttpMethod.GET,  "/rest/atlassian-connect/1/license",  false }, // ported from TestAppPermissions
            new Object[]{ HttpMethod.GET,  "/plugins/servlet/atlassian-connect", false }, // ported from TestAppPermissions
            new Object[]{ HttpMethod.GET,  "/rest/applinks/2.0/entities",        false }
    };
}
