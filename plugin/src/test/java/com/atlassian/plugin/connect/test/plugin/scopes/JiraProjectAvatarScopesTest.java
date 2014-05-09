package com.atlassian.plugin.connect.test.plugin.scopes;

import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.capabilities.ConvertToWiredTest;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@ConvertToWiredTest
@RunWith(Parameterized.class)
public class JiraProjectAvatarScopesTest extends AbstractScopesTest
{
    private static final String PROJECT_AVATAR_URL = "/jira/secure/projectavatar";

    public JiraProjectAvatarScopesTest(ScopeName scope, HttpMethod method, String path, boolean expectedOutcome)
    {
        super(scope, method, path, "", expectedOutcome, "/jira", "JIRA");
    }

    @Parameterized.Parameters(name = "Scope {0}: {1} {2} --> {3}")
    public static Collection<Object[]> testData()
    {
        return Arrays.asList(new Object[][]
        {
            // happy path
            {ScopeName.READ, HttpMethod.GET, PROJECT_AVATAR_URL, true},

            // higher scopes
            {ScopeName.WRITE, HttpMethod.GET, PROJECT_AVATAR_URL, true},
            {ScopeName.DELETE, HttpMethod.GET, PROJECT_AVATAR_URL, true},
            {ScopeName.PROJECT_ADMIN, HttpMethod.GET, PROJECT_AVATAR_URL, true},
            {ScopeName.ADMIN, HttpMethod.GET, PROJECT_AVATAR_URL, true},

            // bad paths
            {ScopeName.READ, HttpMethod.GET, "/jira/secure/projectavatarpagan", false},
            {ScopeName.READ, HttpMethod.GET, "/jira/secure/projectavatar/lordbritish", false},

            // bad method type
            {ScopeName.ADMIN, HttpMethod.POST, PROJECT_AVATAR_URL, false},
            {ScopeName.ADMIN, HttpMethod.PUT, PROJECT_AVATAR_URL, false},
            {ScopeName.ADMIN, HttpMethod.DELETE, PROJECT_AVATAR_URL, false}
        });
    }
}
