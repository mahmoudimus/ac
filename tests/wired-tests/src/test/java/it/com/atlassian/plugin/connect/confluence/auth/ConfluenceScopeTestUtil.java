package it.com.atlassian.plugin.connect.confluence.auth;

import com.atlassian.plugin.connect.api.request.HttpMethod;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import it.com.atlassian.plugin.connect.plugin.auth.scope.ScopeTestData;

public class ConfluenceScopeTestUtil {
    private ConfluenceScopeTestUtil() {
    }

    public static ScopeTestData emptyBodyForConfluence(ScopeName scope, HttpMethod method, String path, boolean expectedOutcome) {
        return new ScopeTestData(scope, method, path, "", expectedOutcome, "/confluence");
    }

    public static ScopeTestData xmlBodyForConfluence(ScopeName scope, String methodName, boolean expectedOutcome) {
        return new ScopeTestData(scope, HttpMethod.POST, "/confluence/rpc/xmlrpc", createXmlRpcPayload(methodName), expectedOutcome, "/confluence");
    }

    private static String createXmlRpcPayload(String methodName) {
        return "<?xml version=\"1.0\"?>\n" +
                "<methodCall>\n" +
                "   <methodName>" + methodName + "</methodName>\n" +
                "</methodCall>";
    }
}
