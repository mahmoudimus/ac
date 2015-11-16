package it.com.atlassian.plugin.connect.jira.auth.scope;

import com.atlassian.plugin.connect.api.request.HttpMethod;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import it.com.atlassian.plugin.connect.plugin.auth.scope.ScopeTestData;

public class JiraScopeTestHelper
{
    public static ScopeTestData emptyBodyForJira(ScopeName scope, HttpMethod method, String path, boolean expectedOutcome)
    {
        return new ScopeTestData(scope, method, path, "", expectedOutcome, "/jira");
    }
}
