package it.com.atlassian.plugin.connect.jira.scopes.manager;

import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.scopes.AddOnScopeManager;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import it.com.atlassian.plugin.connect.plugin.scopes.manager.RequestInApiScopeTest;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static it.com.atlassian.plugin.connect.jira.util.JiraScopeTestHelper.emptyBodyForJira;
import static java.util.Arrays.asList;

@Application ("jira")
@RunWith (AtlassianPluginsTestRunner.class)
public class JiraAgileRestScopesTest extends RequestInApiScopeTest
{
    /**
     * These tests are not exhaustive. They touch parts of JIRA Agile API that was selectively made available to Connect apps.
     */
    public JiraAgileRestScopesTest(AddOnScopeManager scopeManager, TestPluginInstaller testPluginInstaller)
    {
        super(scopeManager, testPluginInstaller, testData());
    }

    public static Collection<ScopeTestData> testData()
    {
        List<ScopeTestData> params = new ArrayList<>();

        params.addAll(asList(
                // RapidView Read
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "/jira/rest/greenhopper/1.0/rapidview", true),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "/jira/rest/greenhopper/1.0/rapidview/123", true),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "/jira/rest/greenhopper/1.0/rapidview/nonnumeric", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.POST, "/jira/rest/greenhopper/1.0/rapidview", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.PUT, "/jira/rest/greenhopper/1.0/rapidview", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.DELETE, "/jira/rest/greenhopper/1.0/rapidview", false),

                emptyBodyForJira(null, HttpMethod.GET, "/jira/rest/greenhopper/1.0/rapidview", false),
                emptyBodyForJira(null, HttpMethod.GET, "/jira/rest/greenhopper/1.0/rapidview/123", false),

                // Plan Backlog READ
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "/jira/rest/greenhopper/1.0/xboard/plan/backlog/data", true),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "/jira/rest/greenhopper/1.0/xboard/plan/backlog/data/any", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "/jira/rest/greenhopper/1.0/xboard/plan/backlog/epics", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "/jira/rest/greenhopper/1.0/xboard/plan/backlog/issue", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "/jira/rest/greenhopper/1.0/xboard/plan/backlog/versions", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "/jira/rest/greenhopper/1.0/xboard/plan/backlog", false),

                emptyBodyForJira(null, HttpMethod.GET, "/jira/rest/greenhopper/1.0/xboard/plan/backlog/data", false),

                // RapidViewConfig EditModel Read
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "/jira/rest/greenhopper/1.0/rapidviewconfig/editmodel", true),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "/jira/rest/greenhopper/1.0/rapidviewconfig/editmodel/any", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.POST, "/jira/rest/greenhopper/1.0/rapidviewconfig/editmodel", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.PUT, "/jira/rest/greenhopper/1.0/rapidviewconfig/editmodel", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.DELETE, "/jira/rest/greenhopper/1.0/rapidviewconfig/editmodel", false),

                emptyBodyForJira(null, HttpMethod.GET, "/jira/rest/greenhopper/1.0/rapidviewconfig/editmodel", false),

                // Global Ranking Write
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.PUT, "/jira/rest/greenhopper/1.0/api/rank/before", true),
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.PUT, "/jira/rest/greenhopper/1.0/api/rank/after", true),
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.PUT, "/jira/rest/greenhopper/1.0/api/rank", false),

                emptyBodyForJira(ScopeName.READ, HttpMethod.PUT, "/jira/rest/greenhopper/1.0/api/rank/before", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.PUT, "/jira/rest/greenhopper/1.0/api/rank/after", false),
                emptyBodyForJira(null, HttpMethod.PUT, "/jira/rest/greenhopper/1.0/api/rank/before", false),
                emptyBodyForJira(null, HttpMethod.PUT, "/jira/rest/greenhopper/1.0/api/rank/after", false),

                // Sprint Ranking Write
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.PUT, "/jira/rest/greenhopper/1.0/sprint/rank", true),
                emptyBodyForJira(ScopeName.READ, HttpMethod.PUT, "/jira/rest/greenhopper/1.0/sprint/rank", false),
                emptyBodyForJira(null, HttpMethod.PUT, "/jira/rest/greenhopper/1.0/sprint/rank", false),

                // epics READ
                emptyBodyForJira(null, HttpMethod.GET, "jira/rest/greenhopper/1.0/epics", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "jira/rest/greenhopper/1.0/epics", true),

                // Add issue to epic WRITE
                emptyBodyForJira(null, HttpMethod.PUT, "jira/rest/greenhopper/1.0/epics/EPIC-42/add", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.PUT, "jira/rest/greenhopper/1.0/epics/EPIC-42/add", false),
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.PUT, "jira/rest/greenhopper/1.0/epics/EPIC-42/add", true)));

        return params;
    }
}
