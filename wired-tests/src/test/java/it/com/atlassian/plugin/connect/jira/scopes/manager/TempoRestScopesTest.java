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
public class TempoRestScopesTest extends ScopeManagerTest
{
    public TempoRestScopesTest(AddOnScopeManager scopeManager, ScopeTestHelper scopeTestHelper)
    {
            super(scopeManager, scopeTestHelper, testData());
    }

    /**
     * These tests are not exhaustive. They are samples across the JIRA endpoints.
     */
    public static Collection<ScopeTestData> testData()
    {
        List<ScopeTestData> params = new ArrayList<>();

        params.addAll(asList(
                // Planning READ
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "/jira/rest/tempo-planning/latest/anything", true),
                emptyBodyForJira(ScopeName.READ, HttpMethod.POST, "/jira/rest/tempo-planning/latest/anything", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.PUT, "/jira/rest/tempo-planning/latest/anything", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.DELETE, "/jira/rest/tempo-planning/latest/anything", false),

                // Teams READ
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "/jira/rest/tempo-teams/latest/anything", true),
                emptyBodyForJira(ScopeName.READ, HttpMethod.POST, "/jira/rest/tempo-teams/latest/anything", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.PUT, "/jira/rest/tempo-teams/latest/anything", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.DELETE, "/jira/rest/tempo-teams/latest/anything", false),

                // Core READ
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "/jira/rest/tempo-core/latest/anything", true),
                emptyBodyForJira(ScopeName.READ, HttpMethod.POST, "/jira/rest/tempo-core/latest/anything", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.PUT, "/jira/rest/tempo-core/latest/anything", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.DELETE, "/jira/rest/tempo-core/latest/anything", false),

                // Accounts READ
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "/jira/rest/tempo-accounts/latest/anything", true),
                emptyBodyForJira(ScopeName.READ, HttpMethod.POST, "/jira/rest/tempo-accounts/latest/anything", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.PUT, "/jira/rest/tempo-accounts/latest/anything", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.DELETE, "/jira/rest/tempo-accounts/latest/anything", false),

                // Planning WRITE
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.GET, "/jira/rest/tempo-planning/latest/anything", true),
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.POST, "/jira/rest/tempo-planning/latest/anything", true),
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.PUT, "/jira/rest/tempo-planning/latest/anything", true),
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.DELETE, "/jira/rest/tempo-planning/latest/anything", false),

                // Teams WRITE
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.GET, "/jira/rest/tempo-teams/latest/anything", true),
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.POST, "/jira/rest/tempo-teams/latest/anything", true),
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.PUT, "/jira/rest/tempo-teams/latest/anything", true),
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.DELETE, "/jira/rest/tempo-teams/latest/anything", false),

                // Core WRITE
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.GET, "/jira/rest/tempo-core/latest/anything", true),
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.POST, "/jira/rest/tempo-core/latest/anything", true),
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.PUT, "/jira/rest/tempo-core/latest/anything", true),
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.DELETE, "/jira/rest/tempo-core/latest/anything", false),

                // Accounts WRITE
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.GET, "/jira/rest/tempo-accounts/latest/anything", true),
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.POST, "/jira/rest/tempo-accounts/latest/anything", true),
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.PUT, "/jira/rest/tempo-accounts/latest/anything", true),
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.DELETE, "/jira/rest/tempo-accounts/latest/anything", false),

                // Planning DELETE
                emptyBodyForJira(ScopeName.DELETE, HttpMethod.GET, "/jira/rest/tempo-planning/latest/anything", true),
                emptyBodyForJira(ScopeName.DELETE, HttpMethod.POST, "/jira/rest/tempo-planning/latest/anything", true),
                emptyBodyForJira(ScopeName.DELETE, HttpMethod.PUT, "/jira/rest/tempo-planning/latest/anything", true),
                emptyBodyForJira(ScopeName.DELETE, HttpMethod.DELETE, "/jira/rest/tempo-planning/latest/anything", true),

                // Teams DELETE
                emptyBodyForJira(ScopeName.DELETE, HttpMethod.GET, "/jira/rest/tempo-teams/latest/anything", true),
                emptyBodyForJira(ScopeName.DELETE, HttpMethod.POST, "/jira/rest/tempo-teams/latest/anything", true),
                emptyBodyForJira(ScopeName.DELETE, HttpMethod.PUT, "/jira/rest/tempo-teams/latest/anything", true),
                emptyBodyForJira(ScopeName.DELETE, HttpMethod.DELETE, "/jira/rest/tempo-teams/latest/anything", true),

                // Core DELETE
                emptyBodyForJira(ScopeName.DELETE, HttpMethod.GET, "/jira/rest/tempo-core/latest/anything", true),
                emptyBodyForJira(ScopeName.DELETE, HttpMethod.POST, "/jira/rest/tempo-core/latest/anything", true),
                emptyBodyForJira(ScopeName.DELETE, HttpMethod.PUT, "/jira/rest/tempo-core/latest/anything", true),
                emptyBodyForJira(ScopeName.DELETE, HttpMethod.DELETE, "/jira/rest/tempo-core/latest/anything", true),

                // Accounts DELETE
                emptyBodyForJira(ScopeName.DELETE, HttpMethod.GET, "/jira/rest/tempo-accounts/latest/anything", true),
                emptyBodyForJira(ScopeName.DELETE, HttpMethod.POST, "/jira/rest/tempo-accounts/latest/anything", true),
                emptyBodyForJira(ScopeName.DELETE, HttpMethod.PUT, "/jira/rest/tempo-accounts/latest/anything", true),
                emptyBodyForJira(ScopeName.DELETE, HttpMethod.DELETE, "/jira/rest/tempo-accounts/latest/anything", true)));

        return params;
    }
}
