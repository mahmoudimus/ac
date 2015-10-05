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
public class JiraRestScopesTest extends ScopeManagerTest
{
    public JiraRestScopesTest(AddOnScopeManager scopeManager, ScopeTestHelper scopeTestHelper)
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
                // "myself": no scopes
                emptyBodyForJira(null, HttpMethod.GET, "/jira/rest/api/2/myself", false),

                // "myself": read scope with various http methods
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "/jira/rest/api/2/myself", true),
                emptyBodyForJira(ScopeName.READ, HttpMethod.POST, "/jira/rest/api/2/myself", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.PUT, "/jira/rest/api/2/myself", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.DELETE, "/jira/api/2/myself", false),

                // "myself": GET with implied scopes
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.GET, "/jira/rest/api/2/myself", true),
                emptyBodyForJira(ScopeName.DELETE, HttpMethod.GET, "/jira/rest/api/2/myself", true),
                emptyBodyForJira(ScopeName.ADMIN, HttpMethod.GET, "/jira/rest/api/2/myself", true),

                // just in case someone ever adds the insanity of getting a password
                emptyBodyForJira(null, HttpMethod.GET, "/jira/rest/api/2/myself/password", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "/jira/rest/api/2/myself/password", false),

                // issueLinkType reads require READ
                emptyBodyForJira(null, HttpMethod.GET, "/jira/rest/api/2/issueLinkType", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "/jira/rest/api/2/issueLinkType", true),
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.GET, "/jira/rest/api/2/issueLinkType", true),
                emptyBodyForJira(ScopeName.DELETE, HttpMethod.GET, "/jira/rest/api/2/issueLinkType", true),
                emptyBodyForJira(ScopeName.ADMIN, HttpMethod.GET, "/jira/rest/api/2/issueLinkType", true),
                emptyBodyForJira(null, HttpMethod.GET, "/jira/rest/api/2/issueLinkType/abc", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "/jira/rest/api/2/issueLinkType/abc", true),
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.GET, "/jira/rest/api/2/issueLinkType/abc", true),
                emptyBodyForJira(ScopeName.DELETE, HttpMethod.GET, "/jira/rest/api/2/issueLinkType/abc", true),
                emptyBodyForJira(ScopeName.ADMIN, HttpMethod.GET, "/jira/rest/api/2/issueLinkType/abc", true),

                // issueLinkType creations and edits require ADMIN
                emptyBodyForJira(null, HttpMethod.POST, "/jira/rest/api/2/issueLinkType", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.POST, "/jira/rest/api/2/issueLinkType", false),
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.POST, "/jira/rest/api/2/issueLinkType", false),
                emptyBodyForJira(ScopeName.DELETE, HttpMethod.POST, "/jira/rest/api/2/issueLinkType", false),
                emptyBodyForJira(ScopeName.ADMIN, HttpMethod.POST, "/jira/rest/api/2/issueLinkType", true),
                emptyBodyForJira(null, HttpMethod.PUT, "/jira/rest/api/2/issueLinkType/abc", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.PUT, "/jira/rest/api/2/issueLinkType/abc", false),
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.PUT, "/jira/rest/api/2/issueLinkType/abc", false),
                emptyBodyForJira(ScopeName.DELETE, HttpMethod.PUT, "/jira/rest/api/2/issueLinkType/abc", false),
                emptyBodyForJira(ScopeName.ADMIN, HttpMethod.PUT, "/jira/rest/api/2/issueLinkType/abc", true),

                // issueLinkType deletes require ADMIN
                emptyBodyForJira(null, HttpMethod.DELETE, "/jira/rest/api/2/issueLinkType/abc", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.DELETE, "/jira/rest/api/2/issueLinkType/abc", false),
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.DELETE, "/jira/rest/api/2/issueLinkType/abc", false),
                emptyBodyForJira(ScopeName.DELETE, HttpMethod.DELETE, "/jira/rest/api/2/issueLinkType/abc", false),
                emptyBodyForJira(ScopeName.ADMIN, HttpMethod.DELETE, "/jira/rest/api/2/issueLinkType/abc", true),

                // issue types get requires READ
                emptyBodyForJira(null, HttpMethod.GET, "jira/rest/api/2/issuetype/2", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "jira/rest/api/2/issuetype/2", true),
                emptyBodyForJira(null, HttpMethod.GET, "jira/rest/api/2/issuetype", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "jira/rest/api/2/issuetype", true),

                // issue type create requires ADMIN
                emptyBodyForJira(null, HttpMethod.POST, "jira/rest/api/2/issuetype", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.POST, "jira/rest/api/2/issuetype", false),
                emptyBodyForJira(ScopeName.DELETE, HttpMethod.POST, "jira/rest/api/2/issuetype", false),
                emptyBodyForJira(ScopeName.PROJECT_ADMIN, HttpMethod.POST, "jira/rest/api/2/issuetype", false),
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.POST, "jira/rest/api/2/issuetype", false),
                emptyBodyForJira(ScopeName.ADMIN, HttpMethod.POST, "jira/rest/api/2/issuetype", true),

                // issue types put requires ADMIN
                emptyBodyForJira(null, HttpMethod.PUT, "jira/rest/api/2/issuetype/2", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.PUT, "jira/rest/api/2/issuetype/2", false),
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.PUT, "jira/rest/api/2/issuetype/2", false),
                emptyBodyForJira(ScopeName.PROJECT_ADMIN, HttpMethod.PUT, "jira/rest/api/2/issuetype/2", false),
                emptyBodyForJira(ScopeName.DELETE, HttpMethod.PUT, "jira/rest/api/2/issuetype/2", false),
                emptyBodyForJira(ScopeName.ADMIN, HttpMethod.PUT, "jira/rest/api/2/issuetype/2", true),

                // issue types delete requires ADMIN
                emptyBodyForJira(null, HttpMethod.DELETE, "jira/rest/api/2/issuetype/2", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.DELETE, "jira/rest/api/2/issuetype/2", false),
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.DELETE, "jira/rest/api/2/issuetype/2", false),
                emptyBodyForJira(ScopeName.PROJECT_ADMIN, HttpMethod.DELETE, "jira/rest/api/2/issuetype/2", false),
                emptyBodyForJira(ScopeName.DELETE, HttpMethod.DELETE, "jira/rest/api/2/issuetype/2", false),
                emptyBodyForJira(ScopeName.ADMIN, HttpMethod.DELETE, "jira/rest/api/2/issuetype/2", true),

                // groups picker requires READ
                emptyBodyForJira(null, HttpMethod.GET, "/jira/rest/api/2/groups/picker", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "/jira/rest/api/2/groups/picker", true),

                // JQL autocomplete require READ
                emptyBodyForJira(null, HttpMethod.GET, "/jira/rest/api/2/jql/autocompletedata", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "/jira/rest/api/2/jql/autocompletedata", true),

                // user picker requires READ
                emptyBodyForJira(null, HttpMethod.GET, "/jira/rest/api/2/user/picker?query", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "/jira/rest/api/2/user/picker?query", true),

                // configuration requires READ
                emptyBodyForJira(null, HttpMethod.GET, "/jira/rest/api/2/configuration", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "/jira/rest/api/2/configuration", true),

                // reading project properties requires READ
                emptyBodyForJira(null, HttpMethod.GET, "/jira/rest/api/2/project/ABC-123/properties/some-property-name", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "/jira/rest/api/2/project/ABC-123/properties/some-property-name", true),

                // writing project properties requires WRITE
                emptyBodyForJira(null, HttpMethod.PUT, "/jira/rest/api/2/project/ABC-123/properties/some-property-name", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.PUT, "/jira/rest/api/2/project/ABC-123/properties/some-property-name", false),
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.PUT, "/jira/rest/api/2/project/ABC-123/properties/some-property-name", true),

                // mutating other project data requires PROJECT_ADMIN
                emptyBodyForJira(ScopeName.DELETE, HttpMethod.PUT, "/jira/rest/api/2/project/ABC-123/role", false),
                emptyBodyForJira(ScopeName.PROJECT_ADMIN, HttpMethod.PUT, "/jira/rest/api/2/project/ABC-123/role", true),
                emptyBodyForJira(ScopeName.DELETE, HttpMethod.PUT, "/jira/rest/api/2/project/ABC-123/avatar", false),
                emptyBodyForJira(ScopeName.PROJECT_ADMIN, HttpMethod.PUT, "/jira/rest/api/2/project/ABC-123/avatar", true),
                emptyBodyForJira(ScopeName.DELETE, HttpMethod.POST, "/jira/rest/api/2/project/ABC-123/avatar", false),
                emptyBodyForJira(ScopeName.PROJECT_ADMIN, HttpMethod.POST, "/jira/rest/api/2/project/ABC-123/avatar", true),
                emptyBodyForJira(ScopeName.DELETE, HttpMethod.DELETE, "/jira/rest/api/2/project/ABC-123/avatar/123", false),
                emptyBodyForJira(ScopeName.PROJECT_ADMIN, HttpMethod.DELETE, "/jira/rest/api/2/project/ABC-123/avatar/123", true)
        ));

        // never allow an add-on to change a user's details or password
        for (ScopeName scopeName : asList(ScopeName.WRITE, ScopeName.DELETE, ScopeName.ADMIN))
        {
            for (HttpMethod httpMethod : asList(HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE))
            {
                params.add(emptyBodyForJira(scopeName, httpMethod, "/jira/rest/api/2/myself", false));
                params.add(emptyBodyForJira(scopeName, httpMethod, "/jira/rest/api/2/myself/password", false));
            }
        }

        return params;
    }

}
