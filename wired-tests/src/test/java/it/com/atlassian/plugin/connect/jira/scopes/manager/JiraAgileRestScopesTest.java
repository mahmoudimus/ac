package it.com.atlassian.plugin.connect.jira.scopes.manager;

import com.atlassian.plugin.connect.api.http.HttpMethod;
import com.atlassian.plugin.connect.api.scopes.AddOnScopeManager;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.testsupport.scopes.ScopeTestHelper;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static it.com.atlassian.plugin.connect.jira.util.JiraScopeTestHelper.emptyBodyForJira;
import static java.util.Arrays.asList;

@Application ("jira")
@RunWith (AtlassianPluginsTestRunner.class)
public class JiraAgileRestScopesTest extends ScopeManagerTest
{
    /**
     * These tests are not exhaustive. They touch parts of JIRA Agile API that was selectively made available to Connect apps.
     */
    public JiraAgileRestScopesTest(AddOnScopeManager scopeManager, ScopeTestHelper scopeTestHelper)
    {
        super(scopeManager, scopeTestHelper, testData());
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
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.PUT, "jira/rest/greenhopper/1.0/epics/EPIC-42/add", true),

                // Move issues to backlog WRITE
                emptyBodyForJira(null, HttpMethod.POST, "jira/rest/agile/1.0/backlog/issue", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.POST, "jira/rest/agile/1.0/backlog/issue", false),
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.POST, "jira/rest/agile/1.0/backlog/issue", true),

                // Get all boards READ
                emptyBodyForJira(null, HttpMethod.GET, "jira/rest/agile/1.0/board", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "jira/rest/agile/1.0/board", true),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "jira/rest/agile/1.0/board?type=Kanban&startAt=4", true),

                // Get single board READ
                emptyBodyForJira(null, HttpMethod.GET, "jira/rest/agile/1.0/board/1", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "jira/rest/agile/1.0/board/1", true),

                // Get board's backlog READ
                emptyBodyForJira(null, HttpMethod.GET, "jira/rest/agile/1.0/board/1/backlog", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "jira/rest/agile/1.0/board/1/backlog", true),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "jira/rest/agile/1.0/board/1/backlog?startAt=0&maxResults=100", true),

                // Get board's issues READ
                emptyBodyForJira(null, HttpMethod.GET, "jira/rest/agile/1.0/board/1/issue", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "jira/rest/agile/1.0/board/1/issue", true),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "jira/rest/agile/1.0/board/1/issue?startAt=0&maxResults=100", true),

                // Get board's configuration READ
                emptyBodyForJira(null, HttpMethod.GET, "jira/rest/agile/1.0/board/1/configuration", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "jira/rest/agile/1.0/board/1/configuration", true),

                // Get board's epics READ
                emptyBodyForJira(null, HttpMethod.GET, "jira/rest/agile/1.0/board/1/epic", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "jira/rest/agile/1.0/board/1/epic", true),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "jira/rest/agile/1.0/board/1/epic?startAt=0&maxResults=100", true),

                // Get board's epic's issues READ
                emptyBodyForJira(null, HttpMethod.GET, "jira/rest/agile/1.0/board/1/epic/2/issue", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "jira/rest/agile/1.0/board/1/epic/2/issue", true),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "jira/rest/agile/1.0/board/1/epic/2/issue?startAt=0&maxResults=100", true),

                // Get board's sprints READ
                emptyBodyForJira(null, HttpMethod.GET, "jira/rest/agile/1.0/board/1/sprint/", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "jira/rest/agile/1.0/board/1/sprint", true),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "jira/rest/agile/1.0/board/1/sprint?startAt=0&maxResults=100", true),

                // Get board's sprint's issues READ
                emptyBodyForJira(null, HttpMethod.GET, "jira/rest/agile/1.0/board/1/sprint/2/issue", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "jira/rest/agile/1.0/board/1/sprint/2/issue", true),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "jira/rest/agile/1.0/board/1/sprint/2/issue?startAt=0&maxResults=100", true),

                // Get board's version READ
                emptyBodyForJira(null, HttpMethod.GET, "jira/rest/agile/1.0/board/1/version", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "jira/rest/agile/1.0/board/1/version", true),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "jira/rest/agile/1.0/board/1/version?startAt=0&maxResults=100", true),

                // Get enchanced issue READ
                emptyBodyForJira(null, HttpMethod.GET, "jira/rest/agile/1.0/issue/1000", false),
                emptyBodyForJira(null, HttpMethod.GET, "jira/rest/agile/1.0/issue/KEY-12", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "jira/rest/agile/1.0/issue/1000", true),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "jira/rest/agile/1.0/issue/KEY-12", true),

                // Rank issues WRTIE
                emptyBodyForJira(null, HttpMethod.PUT, "jira/rest/agile/1.0/issue/rank", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.PUT, "jira/rest/agile/1.0/issue/rank", false),
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.PUT, "jira/rest/agile/1.0/issue/rank", true),

                // Create sprint WRTIE
                emptyBodyForJira(null, HttpMethod.POST, "jira/rest/agile/1.0/sprint", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.POST, "jira/rest/agile/1.0/sprint", false),
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.POST, "jira/rest/agile/1.0/sprint", true),

                // Get sprint READ
                emptyBodyForJira(null, HttpMethod.GET, "jira/rest/agile/1.0/sprint/5", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "jira/rest/agile/1.0/sprint/5", true),

                // Update sprint partialy WRTIE
                emptyBodyForJira(null, HttpMethod.POST, "jira/rest/agile/1.0/sprint/5", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.POST, "jira/rest/agile/1.0/sprint/5", false),
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.POST, "jira/rest/agile/1.0/sprint/5", true),

                // Update sprint WRTIE
                emptyBodyForJira(null, HttpMethod.PUT, "jira/rest/agile/1.0/sprint/5", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.PUT, "jira/rest/agile/1.0/sprint/5", false),
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.PUT, "jira/rest/agile/1.0/sprint/5", true),

                // Delete sprint DELETE
                emptyBodyForJira(null, HttpMethod.POST, "jira/rest/agile/1.0/sprint/5", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.POST, "jira/rest/agile/1.0/sprint/5", false),
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.POST, "jira/rest/agile/1.0/sprint/5", false),
                emptyBodyForJira(ScopeName.DELETE, HttpMethod.POST, "jira/rest/agile/1.0/sprint/5", true),

                // Get issues for sprint READ
                emptyBodyForJira(null, HttpMethod.GET, "jira/rest/agile/1.0/sprint/5/issue", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "jira/rest/agile/1.0/sprint/5/issue", true),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "jira/rest/agile/1.0/sprint/5/issue?startAt=0&maxResults=100", true),

                // Move issues to sprint WRTIE
                emptyBodyForJira(null, HttpMethod.POST, "jira/rest/agile/1.0/sprint/5/issue", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.POST, "jira/rest/agile/1.0/sprint/5/issue", false),
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.POST, "jira/rest/agile/1.0/sprint/5/issue", true)
        ));
        return params;
    }
}
