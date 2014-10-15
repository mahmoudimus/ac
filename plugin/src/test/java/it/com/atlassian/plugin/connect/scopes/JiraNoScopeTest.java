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

@Application("jira")
@ParameterizedWiredTest
public class JiraNoScopeTest extends ScopeTestBase
{
    public JiraNoScopeTest(TestPluginInstaller testPluginInstaller,
            TestAuthenticator testAuthenticator,
            JwtWriterFactory jwtWriterFactory,
            ConnectAddonRegistry connectAddonRegistry,
            ApplicationProperties applicationProperties)
    {
        super(null, testPluginInstaller, testAuthenticator, jwtWriterFactory, connectAddonRegistry, applicationProperties);
    }

    @ParameterizedWiredTest.Parameters(name="httpRequest", length=3)
    protected Object[][] data = new Object[][]
            {
                    // "myself": no scopes
                    new Object[]{null, HttpMethod.GET, "/jira/rest/api/2/myself", false},

                    // "myself": read scope with various http methods
                    new Object[]{ ScopeName.READ, HttpMethod.POST, "/jira/rest/api/2/myself", false},
                    new Object[]{ScopeName.READ, HttpMethod.PUT, "/jira/rest/api/2/myself", false},
                    new Object[]{ScopeName.READ, HttpMethod.DELETE, "/jira/api/2/myself", false},

                    // just in case someone ever adds the insanity of getting a password
                    new Object[]{null, HttpMethod.GET, "/jira/rest/api/2/myself/password", false},
                    new Object[]{ScopeName.READ, HttpMethod.GET, "/jira/rest/api/2/myself/password", false},

                    // issueLinkType reads require READ
                    new Object[]{null, HttpMethod.GET, "/jira/rest/api/2/issueLinkType", false},

                    new Object[]{null, HttpMethod.GET, "/jira/rest/api/2/issueLinkType/abc", false},

                    // issueLinkType creations and edits require ADMIN
                    new Object[]{null, HttpMethod.POST, "/jira/rest/api/2/issueLinkType", false},
                    new Object[]{ScopeName.READ, HttpMethod.POST, "/jira/rest/api/2/issueLinkType", false},
                    new Object[]{ScopeName.WRITE, HttpMethod.POST, "/jira/rest/api/2/issueLinkType", false},
                    new Object[]{ScopeName.DELETE, HttpMethod.POST, "/jira/rest/api/2/issueLinkType", false},

                    new Object[]{null, HttpMethod.PUT, "/jira/rest/api/2/issueLinkType/abc", false},
                    new Object[]{ScopeName.READ, HttpMethod.PUT, "/jira/rest/api/2/issueLinkType/abc", false},
                    new Object[]{ScopeName.WRITE, HttpMethod.PUT, "/jira/rest/api/2/issueLinkType/abc", false},
                    new Object[]{ScopeName.DELETE, HttpMethod.PUT, "/jira/rest/api/2/issueLinkType/abc", false},

                    // issueLinkType deletes require ADMIN
                    new Object[]{null, HttpMethod.DELETE, "/jira/rest/api/2/issueLinkType/abc", false},
                    new Object[]{ScopeName.READ, HttpMethod.DELETE, "/jira/rest/api/2/issueLinkType/abc", false},
                    new Object[]{ScopeName.WRITE, HttpMethod.DELETE, "/jira/rest/api/2/issueLinkType/abc", false},
                    new Object[]{ScopeName.DELETE, HttpMethod.DELETE, "/jira/rest/api/2/issueLinkType/abc", false},

                    // groups picker requires READ
                    new Object[]{null, HttpMethod.GET, "/jira/rest/api/2/groups/picker", false},

                    // JQL autocomplete require READ
                    new Object[]{ null, HttpMethod.GET,  "/jira/rest/api/2/jql/autocompletedata", false },
                    new Object[]{ null, HttpMethod.GET, "/jira/rest/api/2/user/picker?query", false }
            };

}
