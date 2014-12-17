package it.com.atlassian.plugin.connect.scopes;

import com.atlassian.jwt.writer.JwtWriterFactory;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.sal.api.ApplicationProperties;
import it.com.atlassian.plugin.connect.ParameterizedWiredTest;
import it.com.atlassian.plugin.connect.TestAuthenticator;

@ParameterizedWiredTest
public class ReadScopeTest extends ScopeTestBase
{
    public ReadScopeTest(TestPluginInstaller testPluginInstaller,
                         TestAuthenticator testAuthenticator,
                         JwtWriterFactory jwtWriterFactory,
                         ConnectAddonRegistry connectAddonRegistry,
                         ApplicationProperties applicationProperties)
    {
        super(ScopeName.READ, testPluginInstaller, testAuthenticator, jwtWriterFactory, connectAddonRegistry, applicationProperties);
    }

    /**
     * These tests are not exhaustive. They are samples across the cross-product endpoints.
     */
    @ParameterizedWiredTest.Parameters(name="httpRequest", length=4)
    protected Object[][] data = new Object[][]
    {
            // TODO: ACDEV-1333 new Object[]{ HttpMethod.GET,  "/rest/atlassian-connect/1/license", true },
            { HttpMethod.GET,  "/rest/applinks/2.0/entities", true },
            { HttpMethod.POST, "/rest/applinks/2.0/entities", false },
            { HttpMethod.GET,  "/rest/greenhopper/1.0/rapidview", isJiraProduct() },
            { HttpMethod.PUT,  "/rest/greenhopper/1.0/api/rank/before", false }
    };
}
