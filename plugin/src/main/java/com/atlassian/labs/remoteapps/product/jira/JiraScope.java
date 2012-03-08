package com.atlassian.labs.remoteapps.product.jira;

import com.atlassian.labs.remoteapps.modules.permissions.scope.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

import static com.google.common.collect.Iterables.concat;

/**
 *
 */
abstract class JiraScope implements ApiScope
{
    private final RpcEncodedSoapApiScopeHelper soapScopeHelper;
    private final JsonRpcApiScopeHelper jsonrpcScopeHelper;
    private final Iterable<ApiResourceInfo> apiResourceInfo;
    private RestApiScopeHelper restApiScopeHelper;

    protected JiraScope(Collection<String> methods, Collection<RestApiScopeHelper.RestScope> resources)
    {
        soapScopeHelper = new RpcEncodedSoapApiScopeHelper("/rpc/soap/jirasoapservice-v2", "http://soap.rpc.jira.atlassian.com",
                methods);
        jsonrpcScopeHelper = new JsonRpcApiScopeHelper("/rpc/json-rpc/jirasoapservice-v2", methods);
        restApiScopeHelper = new RestApiScopeHelper(resources);
        this.apiResourceInfo = concat(soapScopeHelper.getApiResourceInfos(), jsonrpcScopeHelper.getApiResourceInfos(),
                restApiScopeHelper.getApiResourceInfos());
    }

    @Override
    public boolean allow(HttpServletRequest request, String user)
    {
        return soapScopeHelper.allow(request, user) || jsonrpcScopeHelper.allow(request, user) || restApiScopeHelper.allow(request, user);
    }

    @Override
    public Iterable<ApiResourceInfo> getApiResourceInfos()
    {
        return apiResourceInfo;
    }
}
