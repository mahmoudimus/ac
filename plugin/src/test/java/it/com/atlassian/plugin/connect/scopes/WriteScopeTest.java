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
public class WriteScopeTest extends ScopeTestBase
{
    public WriteScopeTest(TestPluginInstaller testPluginInstaller,
                          TestAuthenticator testAuthenticator,
                          JwtWriterFactory jwtWriterFactory,
                          ConnectAddonRegistry connectAddonRegistry,
                          ApplicationProperties applicationProperties)
    {
        super(ScopeName.WRITE, testPluginInstaller, testAuthenticator, jwtWriterFactory, connectAddonRegistry, applicationProperties);
    }

    /**
     * These tests are not exhaustive. They are samples across the cross-product endpoints.
     */
    @ParameterizedWiredTest.Parameters(name="httpRequest", length=1)
    protected Object[][] data = new Object[][]
    {
            { HttpMethod.PUT,  "/rest/greenhopper/1.0/api/rank/before", true }
    };
}
