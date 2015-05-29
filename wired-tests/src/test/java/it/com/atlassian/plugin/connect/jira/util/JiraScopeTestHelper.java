package it.com.atlassian.plugin.connect.jira.util;

import com.atlassian.plugin.connect.api.http.HttpMethod;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.spi.util.APITestUtil;
import it.com.atlassian.plugin.connect.jira.scopes.manager.ScopeTestData;

public class JiraScopeTestHelper
{
    public static ScopeTestData emptyBodyForJira(ScopeName scope, HttpMethod method, String path, boolean expectedOutcome)
    {
        return new ScopeTestData(scope, method, path, "", expectedOutcome, "/jira");
    }

    public static ScopeTestData rpcJsonBodyForJira(ScopeName scope, HttpMethod method, String path, String rpcMethod, boolean expectedOutcome)
    {
        return new ScopeTestData(scope, method, path, APITestUtil.createJsonRpcPayload(rpcMethod), expectedOutcome, "/jira");
    }

    public static ScopeTestData rpcSoapBodyForJira(ScopeName scope, HttpMethod method, String path, String rpcMethod, boolean expectedOutcome)
    {
        return new ScopeTestData(scope, method, path, APITestUtil.createSoapRpcPayload(rpcMethod), expectedOutcome, "/jira");
    }
}
