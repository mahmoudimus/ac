package com.atlassian.plugin.connect.test.plugin.scopes;

import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
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

                        // Plan Backlog READ
                        {ScopeName.READ, HttpMethod.GET, "/jira/rest/greenhopper/1.0/xboard/plan/backlog/data", true},
                        {ScopeName.READ, HttpMethod.GET, "/jira/rest/greenhopper/1.0/xboard/plan/backlog/data/any", false},
                        {ScopeName.READ, HttpMethod.GET, "/jira/rest/greenhopper/1.0/xboard/plan/backlog/epics", false},
                        {ScopeName.READ, HttpMethod.GET, "/jira/rest/greenhopper/1.0/xboard/plan/backlog/issue", false},
                        {ScopeName.READ, HttpMethod.GET, "/jira/rest/greenhopper/1.0/xboard/plan/backlog/versions", false},
                        {ScopeName.READ, HttpMethod.GET, "/jira/rest/greenhopper/1.0/xboard/plan/backlog", false},

                        // RapidViewConfig EditModel Read
                        {ScopeName.READ, HttpMethod.GET, "/jira/rest/greenhopper/1.0/rapidviewconfig/editmodel", true},
                        {ScopeName.READ, HttpMethod.GET, "/jira/rest/greenhopper/1.0/rapidviewconfig/editmodel/any", false},
                        {ScopeName.READ, HttpMethod.POST, "/jira/rest/greenhopper/1.0/rapidviewconfig/editmodel", false},
                        {ScopeName.READ, HttpMethod.PUT, "/jira/rest/greenhopper/1.0/rapidviewconfig/editmodel", false},
                        {ScopeName.READ, HttpMethod.DELETE, "/jira/rest/greenhopper/1.0/rapidviewconfig/editmodel", false},

                        // Global Ranking Write
                        {ScopeName.WRITE, HttpMethod.PUT, "/jira/rest/greenhopper/1.0/api/rank/before", true},
                        {ScopeName.WRITE, HttpMethod.PUT, "/jira/rest/greenhopper/1.0/api/rank/after", true},
                        {ScopeName.WRITE, HttpMethod.PUT, "/jira/rest/greenhopper/1.0/api/rank", false},

                        // Sprint Ranking Write
                        {ScopeName.WRITE, HttpMethod.PUT, "/jira/rest/greenhopper/1.0/sprint/rank", true},
                }));

        return params;
    }

    public JiraAgileRestScopesTest(ScopeName scope, HttpMethod method, String path, boolean expectedOutcome)
    {
        super(scope, method, path, "", expectedOutcome, "/jira", "JIRA");
    }

}
