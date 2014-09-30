package com.atlassian.plugin.connect.test.plugin.scopes;

import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.capabilities.ConvertToWiredTest;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;

@ConvertToWiredTest
@RunWith(Parameterized.class)
public class JiraRestScopesTest extends AbstractScopesTest
{
    /**
     * These tests are not exhaustive. They are samples across the JIRA endpoints.
     */
    @Parameterized.Parameters(name = "Scope {0}: {1} {2} --> {3}")
    public static Collection<Object[]> testData()
    {
        List<Object[]> params = new ArrayList<Object[]>();

        params.addAll(asList(new Object[][]
                {
                        // "myself": no scopes
                        {null, HttpMethod.GET, "/jira/rest/api/2/myself", false},

                        // "myself": read scope with various http methods
                        {ScopeName.READ, HttpMethod.GET, "/jira/rest/api/2/myself", true},
                        {ScopeName.READ, HttpMethod.POST, "/jira/rest/api/2/myself", false},
                        {ScopeName.READ, HttpMethod.PUT, "/jira/rest/api/2/myself", false},
                        {ScopeName.READ, HttpMethod.DELETE, "/jira/api/2/myself", false},

                        // "myself": GET with implied scopes
                        {ScopeName.WRITE, HttpMethod.GET, "/jira/rest/api/2/myself", true},
                        {ScopeName.DELETE, HttpMethod.GET, "/jira/rest/api/2/myself", true},
                        {ScopeName.ADMIN, HttpMethod.GET, "/jira/rest/api/2/myself", true},

                        // just in case someone ever adds the insanity of getting a password
                        {null, HttpMethod.GET, "/jira/rest/api/2/myself/password", false},
                        {ScopeName.READ, HttpMethod.GET, "/jira/rest/api/2/myself/password", false},

                        // issueLinkType reads require READ
                        {null, HttpMethod.GET, "/jira/rest/api/2/issueLinkType", false},
                        {ScopeName.READ, HttpMethod.GET, "/jira/rest/api/2/issueLinkType", true},
                        {ScopeName.WRITE, HttpMethod.GET, "/jira/rest/api/2/issueLinkType", true},
                        {ScopeName.DELETE, HttpMethod.GET, "/jira/rest/api/2/issueLinkType", true},
                        {ScopeName.ADMIN, HttpMethod.GET, "/jira/rest/api/2/issueLinkType", true},
                        {null, HttpMethod.GET, "/jira/rest/api/2/issueLinkType/abc", false},
                        {ScopeName.READ, HttpMethod.GET, "/jira/rest/api/2/issueLinkType/abc", true},
                        {ScopeName.WRITE, HttpMethod.GET, "/jira/rest/api/2/issueLinkType/abc", true},
                        {ScopeName.DELETE, HttpMethod.GET, "/jira/rest/api/2/issueLinkType/abc", true},
                        {ScopeName.ADMIN, HttpMethod.GET, "/jira/rest/api/2/issueLinkType/abc", true},

                        // issueLinkType creations and edits require ADMIN
                        {null, HttpMethod.POST, "/jira/rest/api/2/issueLinkType", false},
                        {ScopeName.READ, HttpMethod.POST, "/jira/rest/api/2/issueLinkType", false},
                        {ScopeName.WRITE, HttpMethod.POST, "/jira/rest/api/2/issueLinkType", false},
                        {ScopeName.DELETE, HttpMethod.POST, "/jira/rest/api/2/issueLinkType", false},
                        {ScopeName.ADMIN, HttpMethod.POST, "/jira/rest/api/2/issueLinkType", true},
                        {null, HttpMethod.PUT, "/jira/rest/api/2/issueLinkType/abc", false},
                        {ScopeName.READ, HttpMethod.PUT, "/jira/rest/api/2/issueLinkType/abc", false},
                        {ScopeName.WRITE, HttpMethod.PUT, "/jira/rest/api/2/issueLinkType/abc", false},
                        {ScopeName.DELETE, HttpMethod.PUT, "/jira/rest/api/2/issueLinkType/abc", false},
                        {ScopeName.ADMIN, HttpMethod.PUT, "/jira/rest/api/2/issueLinkType/abc", true},

                        // issueLinkType deletes require ADMIN
                        {null, HttpMethod.DELETE, "/jira/rest/api/2/issueLinkType/abc", false},
                        {ScopeName.READ, HttpMethod.DELETE, "/jira/rest/api/2/issueLinkType/abc", false},
                        {ScopeName.WRITE, HttpMethod.DELETE, "/jira/rest/api/2/issueLinkType/abc", false},
                        {ScopeName.DELETE, HttpMethod.DELETE, "/jira/rest/api/2/issueLinkType/abc", false},
                        {ScopeName.ADMIN, HttpMethod.DELETE, "/jira/rest/api/2/issueLinkType/abc", true},

                        // groups picker requires READ
                        {null, HttpMethod.GET, "/jira/rest/api/2/groups/picker", false},
                        {ScopeName.READ, HttpMethod.GET, "/jira/rest/api/2/groups/picker", true},

                        // JQL autocomplete require READ
                        { null, HttpMethod.GET,  "/jira/rest/api/2/jql/autocompletedata", false },
                        { ScopeName.READ, HttpMethod.GET,  "/jira/rest/api/2/jql/autocompletedata", true }
                }));

        // never allow an add-on to change a user's details or password
        for (ScopeName scopeName : asList(ScopeName.WRITE, ScopeName.DELETE, ScopeName.ADMIN))
        {
            for (HttpMethod httpMethod : asList(HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE))
            {
                params.add(new Object[]{scopeName, httpMethod, "/jira/rest/api/2/myself", false});
                params.add(new Object[]{scopeName, httpMethod, "/jira/rest/api/2/myself/password", false});
            }
        }

        return params;
    }

    public JiraRestScopesTest(ScopeName scope, HttpMethod method, String path, boolean expectedOutcome)
    {
        super(scope, method, path, "", expectedOutcome, "/jira", "JIRA");
    }

}
