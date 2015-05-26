package it.com.atlassian.plugin.connect.jira.scopes.manager;

import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.scopes.AddOnScopeManager;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import it.com.atlassian.plugin.connect.plugin.scopes.manager.RequestInApiScopeTest;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collection;

import static it.com.atlassian.plugin.connect.jira.util.JiraScopeTestHelper.emptyBodyForJira;

@Application ("jira")
@RunWith (AtlassianPluginsTestRunner.class)
public class JiraDownloadScopesTest extends RequestInApiScopeTest
{
    public JiraDownloadScopesTest(AddOnScopeManager scopeManager, TestPluginInstaller testPluginInstaller)
    {
        super(scopeManager, testPluginInstaller, testData());
    }

    public static Collection<ScopeTestData> testData()
    {
        // this is a small scope so the test is exhaustive
        return Arrays.asList(
                // basic case
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "/jira/secure/attachment", true),

                // suffixes
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "/jira/secure/attachment/", true),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "/jira/secure/attachment/1234", true),

                // higher scopes
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.GET, "/jira/secure/attachment", true),
                emptyBodyForJira(ScopeName.DELETE, HttpMethod.GET, "/jira/secure/attachment", true),
                emptyBodyForJira(ScopeName.PROJECT_ADMIN, HttpMethod.GET, "/jira/secure/attachment", true),
                emptyBodyForJira(ScopeName.ADMIN, HttpMethod.GET, "/jira/secure/attachment", true),

                // one-thing-wrong cases
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "/jira/different", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "/jira/secure/ATTACHMENT", false),
                emptyBodyForJira(null, HttpMethod.GET, "/jira/secure/attachment", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.POST, "/jira/secure/attachment", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.PUT, "/jira/secure/attachment", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.DELETE, "/jira/secure/attachment", false));
    }
}
