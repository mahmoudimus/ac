package it.com.atlassian.plugin.connect.jira.auth.scope;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.atlassian.plugin.connect.api.request.HttpMethod;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.auth.scope.AddonScopeManager;
import com.atlassian.plugin.connect.testsupport.scopes.ScopeTestHelper;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;

import org.junit.runner.RunWith;

import it.com.atlassian.plugin.connect.plugin.auth.scope.ScopeManagerTest;
import it.com.atlassian.plugin.connect.plugin.auth.scope.ScopeTestData;

import static it.com.atlassian.plugin.connect.jira.auth.scope.JiraScopeTestHelper.emptyBodyForJira;
import static java.util.Arrays.asList;

@Application("jira")
@RunWith(AtlassianPluginsTestRunner.class)
public class JiraSoftwareRestScopesTest extends ScopeManagerTest {

    /**
     * These tests are not exhaustive. They touch parts of JIRA Agile API that was selectively made available to Connect apps.
     */
    public JiraSoftwareRestScopesTest(AddonScopeManager scopeManager, ScopeTestHelper scopeTestHelper) {
        super(scopeManager, scopeTestHelper, testData());
    }

    public static Collection<ScopeTestData> testData() {
        List<ScopeTestData> params = new ArrayList<>();

        final String publicSoftwareApiPath = "jira/rest/agile/1.0/board";

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

                // Remove issue from epic WRITE
                emptyBodyForJira(null, HttpMethod.PUT, "jira/rest/greenhopper/1.0/epics/remove", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.PUT, "jira/rest/greenhopper/1.0/epics/remove", false),
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.PUT, "jira/rest/greenhopper/1.0/epics/remove", true),

                // All public API methods
                emptyBodyForJira(null, HttpMethod.GET, publicSoftwareApiPath, false),
                emptyBodyForJira(null, HttpMethod.POST, publicSoftwareApiPath, false),
                emptyBodyForJira(null, HttpMethod.PUT, publicSoftwareApiPath, false),
                emptyBodyForJira(null, HttpMethod.DELETE, publicSoftwareApiPath, false),

                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, publicSoftwareApiPath, true),
                emptyBodyForJira(ScopeName.READ, HttpMethod.POST, publicSoftwareApiPath, false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.PUT, publicSoftwareApiPath, false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.DELETE, publicSoftwareApiPath, false),

                emptyBodyForJira(ScopeName.WRITE, HttpMethod.GET, publicSoftwareApiPath, true),
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.POST, publicSoftwareApiPath, true),
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.PUT, publicSoftwareApiPath, true),
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.DELETE, publicSoftwareApiPath, false),

                emptyBodyForJira(ScopeName.DELETE, HttpMethod.GET, publicSoftwareApiPath, true),
                emptyBodyForJira(ScopeName.DELETE, HttpMethod.POST, publicSoftwareApiPath, true),
                emptyBodyForJira(ScopeName.DELETE, HttpMethod.PUT, publicSoftwareApiPath, true),
                emptyBodyForJira(ScopeName.DELETE, HttpMethod.DELETE, publicSoftwareApiPath, true)
        ));
        return params;
    }
}
