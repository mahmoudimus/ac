package it.com.atlassian.plugin.connect.confluence.scopes.manager;

import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.api.http.HttpMethod;
import com.atlassian.plugin.connect.spi.util.APITestUtil;

public class ConfluenceScopeTestUtil
{
    private ConfluenceScopeTestUtil() {}

    public static ScopeTestData emptyBodyForConfluence(ScopeName scope, HttpMethod method, String path, boolean expectedOutcome)
    {
        return new ScopeTestData(scope, method, path, "", expectedOutcome, "/confluence");
    }

    public static ScopeTestData xmlBodyForConfluence(ScopeName scope, String methodName, boolean expectedOutcome)
    {
        return new ScopeTestData(scope, HttpMethod.POST, "/confluence/rpc/xmlrpc", APITestUtil.createXmlRpcPayload(methodName), expectedOutcome, "/confluence");
    }
}
