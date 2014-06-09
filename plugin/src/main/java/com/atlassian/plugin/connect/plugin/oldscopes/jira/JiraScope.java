package com.atlassian.plugin.connect.plugin.oldscopes.jira;

import com.atlassian.plugin.connect.spi.XmlDescriptor;
import com.atlassian.plugin.connect.spi.permission.AbstractPermission;
import com.atlassian.plugin.connect.spi.permission.scope.*;
import com.atlassian.sal.api.user.UserKey;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.concat;

@XmlDescriptor
abstract class JiraScope extends AbstractPermission implements ApiScope
{
    private final RpcEncodedSoapApiScopeHelper soapScopeHelper;
    private final JsonRpcApiScopeHelper jsonRpcScopeHelper;
    private final Iterable<ApiResourceInfo> apiResourceInfo;
    private final RestApiScopeHelper restApiScopeHelper;

    protected JiraScope(String key, Collection<String> methods, Collection<RestApiScopeHelper.RestScope> resources)
    {
        super(key);
        this.soapScopeHelper = new RpcEncodedSoapApiScopeHelper("/rpc/soap/jirasoapservice-v2", "http://soap.rpc.jira.atlassian.com", checkNotNull(methods));
        this.jsonRpcScopeHelper = new JsonRpcApiScopeHelper("/rpc/json-rpc/jirasoapservice-v2", methods);
        this.restApiScopeHelper = new RestApiScopeHelper(checkNotNull(resources));
        this.apiResourceInfo = concat(soapScopeHelper.getApiResourceInfos(), jsonRpcScopeHelper.getApiResourceInfos(), restApiScopeHelper.getApiResourceInfos());
    }

    @Override
    public final boolean allow(HttpServletRequest request, UserKey user)
    {
        return soapScopeHelper.allow(request, user) || jsonRpcScopeHelper.allow(request, user) || restApiScopeHelper.allow(request, user);
    }

    @Override
    public final Iterable<ApiResourceInfo> getApiResourceInfos()
    {
        return apiResourceInfo;
    }
}
