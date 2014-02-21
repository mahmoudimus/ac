package com.atlassian.plugin.connect.test.plugin.scopes;

import com.atlassian.plugin.connect.plugin.capabilities.ConvertToWiredTest;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@ConvertToWiredTest
@RunWith(Parameterized.class)
public class JiraDownloadScopesTest extends AbstractScopesTest
{
    public JiraDownloadScopesTest(ScopeName scope, HttpMethod method, String path, boolean expectedOutcome)
    {
        super(scope, method, path, "", expectedOutcome, "/jira", "JIRA");
    }

    @Parameterized.Parameters(name = "Scope {0}: {1} {2} --> {3}")
    public static Collection<Object[]> testData()
    {
        // this is a small scope so the test is exhaustive
        return Arrays.asList(new Object[][]
        {
                // basic case
                {ScopeName.READ, HttpMethod.GET, "/jira/secure/attachment", true},

                // suffixes
                {ScopeName.READ, HttpMethod.GET, "/jira/secure/attachment/", true},
                {ScopeName.READ, HttpMethod.GET, "/jira/secure/attachment/1234", true},

                // higher scopes
                {ScopeName.WRITE, HttpMethod.GET, "/jira/secure/attachment", true},
                {ScopeName.DELETE, HttpMethod.GET, "/jira/secure/attachment", true},
                {ScopeName.PROJECT_ADMIN, HttpMethod.GET, "/jira/secure/attachment", true},
                {ScopeName.ADMIN, HttpMethod.GET, "/jira/secure/attachment", true},

                // one-thing-wrong cases
                {ScopeName.READ, HttpMethod.GET, "/jira/different", false},
                {ScopeName.READ, HttpMethod.GET, "/jira/secure/ATTACHMENT", false},
                {null, HttpMethod.GET, "/jira/secure/attachment", false},
                {ScopeName.READ, HttpMethod.POST, "/jira/secure/attachment", false},
                {ScopeName.READ, HttpMethod.PUT, "/jira/secure/attachment", false},
                {ScopeName.READ, HttpMethod.DELETE, "/jira/secure/attachment", false}
        });
    }
}
