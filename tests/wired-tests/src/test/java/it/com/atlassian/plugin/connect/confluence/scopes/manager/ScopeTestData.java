package it.com.atlassian.plugin.connect.confluence.scopes.manager;

import com.atlassian.plugin.connect.api.http.HttpMethod;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;

public class ScopeTestData
{
    public final ScopeName scope;
    public final HttpMethod method;
    public final String path;
    public final String requestBody;
    public final boolean expectedOutcome;
    public final String contextPath;

    public ScopeTestData(final ScopeName scope, final HttpMethod method, final String path, final String requestBody, final boolean expectedOutcome, final String contextPath)
    {
        this.scope = scope;
        this.method = method;
        this.path = path;
        this.requestBody = requestBody;
        this.expectedOutcome = expectedOutcome;
        this.contextPath = contextPath;
    }

    @Override
    public String toString()
    {
        return "ScopeTestData{" +
                "scope=" + scope +
                ", method=" + method +
                ", path='" + path + '\'' +
                ", requestBody='" + requestBody + '\'' +
                ", expectedOutcome=" + expectedOutcome +
                ", contextPath='" + contextPath + '\'' +
                '}';
    }
}