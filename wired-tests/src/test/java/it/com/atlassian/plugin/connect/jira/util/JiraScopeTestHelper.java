package it.com.atlassian.plugin.connect.jira.util;

import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import it.com.atlassian.plugin.connect.plugin.scopes.manager.RequestInApiScopeTest;
import it.com.atlassian.plugin.connect.util.APITestUtil;

public class JiraScopeTestHelper
{
    public static RequestInApiScopeTest.ScopeTestData emptyBodyForJira(ScopeName scope, HttpMethod method, String path, boolean expectedOutcome)
    {
        return new RequestInApiScopeTest.ScopeTestData(scope, method, path, "", expectedOutcome, "/jira");
    }

    public static RequestInApiScopeTest.ScopeTestData rpcBodyForJira(ScopeName scope, HttpMethod method, String path, String rpcMethod, boolean expectedOutcome)
    {
        return new RequestInApiScopeTest.ScopeTestData(scope, method, path, APITestUtil.createJsonRpcPayload(rpcMethod), expectedOutcome, "/jira");
    }
}
