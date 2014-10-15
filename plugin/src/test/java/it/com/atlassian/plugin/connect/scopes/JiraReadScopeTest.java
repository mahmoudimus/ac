package it.com.atlassian.plugin.connect.scopes;

import com.atlassian.jwt.writer.JwtWriterFactory;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.sal.api.ApplicationProperties;
import it.com.atlassian.plugin.connect.ParameterizedWiredTest;
import it.com.atlassian.plugin.connect.TestAuthenticator;

@Application ("jira")
@ParameterizedWiredTest
public class JiraReadScopeTest extends ScopeTestBase
{
    public JiraReadScopeTest(TestPluginInstaller testPluginInstaller,
            TestAuthenticator testAuthenticator,
            JwtWriterFactory jwtWriterFactory,
            ConnectAddonRegistry connectAddonRegistry,
            ApplicationProperties applicationProperties)
    {
        super(ScopeName.READ, testPluginInstaller, testAuthenticator, jwtWriterFactory, connectAddonRegistry, applicationProperties);
    }


    @ParameterizedWiredTest.Parameters(name="httpRequest", length=2)
    protected Object[][] data = new Object[][]
            {
                    new Object[]{ HttpMethod.GET, "/jira/rest/api/2/user/picker?query", true },
                    new Object[]{ HttpMethod.GET, "/jira/rest/api/2/groups/picker", true},
                    new Object[]{ HttpMethod.GET, "/jira/rest/api/2/myself", true},
                    new Object[]{ HttpMethod.GET, "/jira/rest/api/2/myself", true},
                    new Object[]{ HttpMethod.GET, "/jira/rest/api/2/issueLinkType", true},
                    new Object[]{ HttpMethod.GET, "/jira/rest/api/2/issueLinkType/abc", true}

            };
}
