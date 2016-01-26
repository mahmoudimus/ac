package it.com.atlassian.plugin.connect.jira.auth.scope;

import com.atlassian.plugin.connect.api.request.HttpMethod;
import com.atlassian.plugin.connect.plugin.auth.scope.AddonScopeManager;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.testsupport.scopes.ScopeTestHelper;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.google.common.collect.ImmutableList;
import it.com.atlassian.plugin.connect.plugin.auth.scope.ScopeTestData;
import it.com.atlassian.plugin.connect.plugin.auth.scope.ScopeManagerTest;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static it.com.atlassian.plugin.connect.jira.auth.scope.JiraScopeTestHelper.emptyBodyForJira;
import static java.util.Arrays.asList;

@Application ("jira")
@RunWith (AtlassianPluginsTestRunner.class)
public class TempoRestScopesTest extends ScopeManagerTest
{
    public TempoRestScopesTest(AddonScopeManager scopeManager, ScopeTestHelper scopeTestHelper)
    {
            super(scopeManager, scopeTestHelper, testData());
    }

    /**
     * These tests are not exhaustive. They are samples across the JIRA endpoints.
     */
    public static Collection<ScopeTestData> testData()
    {
        return ImmutableList.<ScopeTestData>builder()
                .addAll(testDataForResource("tempo-planning"))
                .addAll(testDataForResource("tempo-teams"))
                .addAll(testDataForResource("tempo-core"))
                .addAll(testDataForResource("tempo-accounts"))
                .addAll(testDataForResource("tempo-migration"))
                .build();
    }


    private static List<ScopeTestData> testDataForResource(String resource) {

        String path = "/jira/rest/" + resource + "/latest/anything";

        return ImmutableList.<ScopeTestData>builder()
                .add(emptyBodyForJira(ScopeName.READ, HttpMethod.GET, path, true))
                .add(emptyBodyForJira(ScopeName.READ, HttpMethod.POST, path, false))
                .add(emptyBodyForJira(ScopeName.READ, HttpMethod.PUT, path, false))
                .add(emptyBodyForJira(ScopeName.READ, HttpMethod.DELETE, path, false))

                .add(emptyBodyForJira(ScopeName.WRITE, HttpMethod.GET, path, true))
                .add(emptyBodyForJira(ScopeName.WRITE, HttpMethod.POST, path, true))
                .add(emptyBodyForJira(ScopeName.WRITE, HttpMethod.PUT, path, true))
                .add(emptyBodyForJira(ScopeName.WRITE, HttpMethod.DELETE, path, false))

                .add(emptyBodyForJira(ScopeName.DELETE, HttpMethod.GET, path, true))
                .add(emptyBodyForJira(ScopeName.DELETE, HttpMethod.POST, path, true))
                .add(emptyBodyForJira(ScopeName.DELETE, HttpMethod.PUT, path, true))
                .add(emptyBodyForJira(ScopeName.DELETE, HttpMethod.DELETE, path, true))

                .build();
    }
}
