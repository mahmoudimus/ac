package com.atlassian.plugin.connect.jira.scopes;

import com.atlassian.plugin.connect.jira.scope.JiraScopeProvider;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;

@RunWith(Parameterized.class)
public class TempoRestScopesTest extends AbstractScopesTest
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
                        // Planning READ
                        {ScopeName.READ, HttpMethod.GET, "/jira/rest/tempo-planning/latest/anything", true},
                        {ScopeName.READ, HttpMethod.POST, "/jira/rest/tempo-planning/latest/anything", false},
                        {ScopeName.READ, HttpMethod.PUT, "/jira/rest/tempo-planning/latest/anything", false},
                        {ScopeName.READ, HttpMethod.DELETE, "/jira/rest/tempo-planning/latest/anything", false},

                        // Teams READ
                        {ScopeName.READ, HttpMethod.GET, "/jira/rest/tempo-teams/latest/anything", true},
                        {ScopeName.READ, HttpMethod.POST, "/jira/rest/tempo-teams/latest/anything", false},
                        {ScopeName.READ, HttpMethod.PUT, "/jira/rest/tempo-teams/latest/anything", false},
                        {ScopeName.READ, HttpMethod.DELETE, "/jira/rest/tempo-teams/latest/anything", false},

                        // Core READ
                        {ScopeName.READ, HttpMethod.GET, "/jira/rest/tempo-core/latest/anything", true},
                        {ScopeName.READ, HttpMethod.POST, "/jira/rest/tempo-core/latest/anything", false},
                        {ScopeName.READ, HttpMethod.PUT, "/jira/rest/tempo-core/latest/anything", false},
                        {ScopeName.READ, HttpMethod.DELETE, "/jira/rest/tempo-core/latest/anything", false},

                        // Accounts READ
                        {ScopeName.READ, HttpMethod.GET, "/jira/rest/tempo-accounts/latest/anything", true},
                        {ScopeName.READ, HttpMethod.POST, "/jira/rest/tempo-accounts/latest/anything", false},
                        {ScopeName.READ, HttpMethod.PUT, "/jira/rest/tempo-accounts/latest/anything", false},
                        {ScopeName.READ, HttpMethod.DELETE, "/jira/rest/tempo-accounts/latest/anything", false},

                        // Planning WRITE
                        {ScopeName.WRITE, HttpMethod.GET, "/jira/rest/tempo-planning/latest/anything", true},
                        {ScopeName.WRITE, HttpMethod.POST, "/jira/rest/tempo-planning/latest/anything", true},
                        {ScopeName.WRITE, HttpMethod.PUT, "/jira/rest/tempo-planning/latest/anything", true},
                        {ScopeName.WRITE, HttpMethod.DELETE, "/jira/rest/tempo-planning/latest/anything", false},

                        // Teams WRITE
                        {ScopeName.WRITE, HttpMethod.GET, "/jira/rest/tempo-teams/latest/anything", true},
                        {ScopeName.WRITE, HttpMethod.POST, "/jira/rest/tempo-teams/latest/anything", true},
                        {ScopeName.WRITE, HttpMethod.PUT, "/jira/rest/tempo-teams/latest/anything", true},
                        {ScopeName.WRITE, HttpMethod.DELETE, "/jira/rest/tempo-teams/latest/anything", false},

                        // Core WRITE
                        {ScopeName.WRITE, HttpMethod.GET, "/jira/rest/tempo-core/latest/anything", true},
                        {ScopeName.WRITE, HttpMethod.POST, "/jira/rest/tempo-core/latest/anything", true},
                        {ScopeName.WRITE, HttpMethod.PUT, "/jira/rest/tempo-core/latest/anything", true},
                        {ScopeName.WRITE, HttpMethod.DELETE, "/jira/rest/tempo-core/latest/anything", false},

                        // Accounts WRITE
                        {ScopeName.WRITE, HttpMethod.GET, "/jira/rest/tempo-accounts/latest/anything", true},
                        {ScopeName.WRITE, HttpMethod.POST, "/jira/rest/tempo-accounts/latest/anything", true},
                        {ScopeName.WRITE, HttpMethod.PUT, "/jira/rest/tempo-accounts/latest/anything", true},
                        {ScopeName.WRITE, HttpMethod.DELETE, "/jira/rest/tempo-accounts/latest/anything", false},

                        // Planning DELETE
                        {ScopeName.DELETE, HttpMethod.GET, "/jira/rest/tempo-planning/latest/anything", true},
                        {ScopeName.DELETE, HttpMethod.POST, "/jira/rest/tempo-planning/latest/anything", true},
                        {ScopeName.DELETE, HttpMethod.PUT, "/jira/rest/tempo-planning/latest/anything", true},
                        {ScopeName.DELETE, HttpMethod.DELETE, "/jira/rest/tempo-planning/latest/anything", true},

                        // Teams DELETE
                        {ScopeName.DELETE, HttpMethod.GET, "/jira/rest/tempo-teams/latest/anything", true},
                        {ScopeName.DELETE, HttpMethod.POST, "/jira/rest/tempo-teams/latest/anything", true},
                        {ScopeName.DELETE, HttpMethod.PUT, "/jira/rest/tempo-teams/latest/anything", true},
                        {ScopeName.DELETE, HttpMethod.DELETE, "/jira/rest/tempo-teams/latest/anything", true},

                        // Core DELETE
                        {ScopeName.DELETE, HttpMethod.GET, "/jira/rest/tempo-core/latest/anything", true},
                        {ScopeName.DELETE, HttpMethod.POST, "/jira/rest/tempo-core/latest/anything", true},
                        {ScopeName.DELETE, HttpMethod.PUT, "/jira/rest/tempo-core/latest/anything", true},
                        {ScopeName.DELETE, HttpMethod.DELETE, "/jira/rest/tempo-core/latest/anything", true},

                        // Accounts DELETE
                        {ScopeName.DELETE, HttpMethod.GET, "/jira/rest/tempo-accounts/latest/anything", true},
                        {ScopeName.DELETE, HttpMethod.POST, "/jira/rest/tempo-accounts/latest/anything", true},
                        {ScopeName.DELETE, HttpMethod.PUT, "/jira/rest/tempo-accounts/latest/anything", true},
                        {ScopeName.DELETE, HttpMethod.DELETE, "/jira/rest/tempo-accounts/latest/anything", true},
                }));

        return params;
    }

    public TempoRestScopesTest(ScopeName scope, HttpMethod method, String path, boolean expectedOutcome)
    {
        super(scope, method, path, "", expectedOutcome, "/jira", new JiraScopeProvider());
    }

}
