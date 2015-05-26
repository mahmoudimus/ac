package it.com.atlassian.plugin.connect.confluence.scopes.manager;

import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import it.com.atlassian.plugin.connect.plugin.scopes.manager.RequestInApiScopeTest;
import it.com.atlassian.plugin.connect.util.APITestUtil;

public class ConfluenceScopeTestUtil
{
    private ConfluenceScopeTestUtil() {}

    public static RequestInApiScopeTest.ScopeTestData emptyBodyForConfluence(ScopeName scope, HttpMethod method, String path, boolean expectedOutcome)
    {
        return new RequestInApiScopeTest.ScopeTestData(scope, method, path, "", expectedOutcome, "/confluence");
    }

    public static RequestInApiScopeTest.ScopeTestData xmlBodyForConfluence(ScopeName scope, String methodName, boolean expectedOutcome)
    {
        return new RequestInApiScopeTest.ScopeTestData(scope, HttpMethod.POST, "/confluence/rpc/xmlrpc", APITestUtil.createXmlRpcPayload(methodName), expectedOutcome, "/confluence");
    }
}
