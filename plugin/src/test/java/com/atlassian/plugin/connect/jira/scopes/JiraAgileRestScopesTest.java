package com.atlassian.plugin.connect.jira.scopes;

import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.plugin.connect.test.plugin.scopes.AbstractScopesTest;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;

@RunWith(Parameterized.class)
public class JiraAgileRestScopesTest extends AbstractScopesTest
{
    /**
     * These tests are not exhaustive. They touch parts of JIRA Agile API that was selectively made available to Connect apps.
     */
    @Parameterized.Parameters(name = "Scope {0}: {1} {2} --> {3}")
    public static Collection<Object[]> testData()
    {
        List<Object[]> params = new ArrayList<Object[]>();

        params.addAll(asList(new Object[][]
                {
                        // RapidView Read
                        {ScopeName.READ, HttpMethod.GET, "/jira/rest/greenhopper/1.0/rapidview", true},
                        {ScopeName.READ, HttpMethod.GET, "/jira/rest/greenhopper/1.0/rapidview/123", true},
                        {ScopeName.READ, HttpMethod.GET, "/jira/rest/greenhopper/1.0/rapidview/nonnumeric", false},
                        {ScopeName.READ, HttpMethod.POST, "/jira/rest/greenhopper/1.0/rapidview", false},
                        {ScopeName.READ, HttpMethod.PUT, "/jira/rest/greenhopper/1.0/rapidview", false},
                        {ScopeName.READ, HttpMethod.DELETE, "/jira/rest/greenhopper/1.0/rapidview", false},

                        {null, HttpMethod.GET, "/jira/rest/greenhopper/1.0/rapidview", false},
                        {null, HttpMethod.GET, "/jira/rest/greenhopper/1.0/rapidview/123", false},

                        // Plan Backlog READ
                        {ScopeName.READ, HttpMethod.GET, "/jira/rest/greenhopper/1.0/xboard/plan/backlog/data", true},
                        {ScopeName.READ, HttpMethod.GET, "/jira/rest/greenhopper/1.0/xboard/plan/backlog/data/any", false},
                        {ScopeName.READ, HttpMethod.GET, "/jira/rest/greenhopper/1.0/xboard/plan/backlog/epics", false},
                        {ScopeName.READ, HttpMethod.GET, "/jira/rest/greenhopper/1.0/xboard/plan/backlog/issue", false},
                        {ScopeName.READ, HttpMethod.GET, "/jira/rest/greenhopper/1.0/xboard/plan/backlog/versions", false},
                        {ScopeName.READ, HttpMethod.GET, "/jira/rest/greenhopper/1.0/xboard/plan/backlog", false},

                        {null, HttpMethod.GET, "/jira/rest/greenhopper/1.0/xboard/plan/backlog/data", false},

                        // RapidViewConfig EditModel Read
                        {ScopeName.READ, HttpMethod.GET, "/jira/rest/greenhopper/1.0/rapidviewconfig/editmodel", true},
                        {ScopeName.READ, HttpMethod.GET, "/jira/rest/greenhopper/1.0/rapidviewconfig/editmodel/any", false},
                        {ScopeName.READ, HttpMethod.POST, "/jira/rest/greenhopper/1.0/rapidviewconfig/editmodel", false},
                        {ScopeName.READ, HttpMethod.PUT, "/jira/rest/greenhopper/1.0/rapidviewconfig/editmodel", false},
                        {ScopeName.READ, HttpMethod.DELETE, "/jira/rest/greenhopper/1.0/rapidviewconfig/editmodel", false},

                        {null, HttpMethod.GET, "/jira/rest/greenhopper/1.0/rapidviewconfig/editmodel", false},

                        // Global Ranking Write
                        {ScopeName.WRITE, HttpMethod.PUT, "/jira/rest/greenhopper/1.0/api/rank/before", true},
                        {ScopeName.WRITE, HttpMethod.PUT, "/jira/rest/greenhopper/1.0/api/rank/after", true},
                        {ScopeName.WRITE, HttpMethod.PUT, "/jira/rest/greenhopper/1.0/api/rank", false},

                        {ScopeName.READ, HttpMethod.PUT, "/jira/rest/greenhopper/1.0/api/rank/before", false},
                        {ScopeName.READ, HttpMethod.PUT, "/jira/rest/greenhopper/1.0/api/rank/after", false},
                        {null, HttpMethod.PUT, "/jira/rest/greenhopper/1.0/api/rank/before", false},
                        {null, HttpMethod.PUT, "/jira/rest/greenhopper/1.0/api/rank/after", false},

                        // Sprint Ranking Write
                        {ScopeName.WRITE, HttpMethod.PUT, "/jira/rest/greenhopper/1.0/sprint/rank", true},
                        {ScopeName.READ, HttpMethod.PUT, "/jira/rest/greenhopper/1.0/sprint/rank", false},
                        {null, HttpMethod.PUT, "/jira/rest/greenhopper/1.0/sprint/rank", false},


                        // epics READ
                        {null, HttpMethod.GET, "jira/rest/greenhopper/1.0/epics", false},
                        {ScopeName.READ, HttpMethod.GET, "jira/rest/greenhopper/1.0/epics", true},

                        // Add issue to epic WRITE
                        {null, HttpMethod.PUT, "jira/rest/greenhopper/1.0/epics/EPIC-42/add", false},
                        {ScopeName.READ, HttpMethod.PUT, "jira/rest/greenhopper/1.0/epics/EPIC-42/add", false},
                        {ScopeName.WRITE, HttpMethod.PUT, "jira/rest/greenhopper/1.0/epics/EPIC-42/add", true},
                }));

        return params;
    }

    public JiraAgileRestScopesTest(ScopeName scope, HttpMethod method, String path, boolean expectedOutcome)
    {
        super(scope, method, path, "", expectedOutcome, "/jira", "JIRA");
    }

}
