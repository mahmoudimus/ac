package com.atlassian.plugin.remotable.plugin.oldscopes.jira;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.plugin.remotable.api.InstallationMode;
import com.atlassian.plugin.remotable.spi.permission.AbstractPermission;
import com.atlassian.plugin.remotable.spi.permission.scope.*;

import com.google.common.collect.ImmutableSet;

import static com.atlassian.plugin.remotable.api.InstallationMode.REMOTE;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.concat;

abstract class JiraScope extends AbstractPermission implements ApiScope
{
    private final RpcEncodedSoapApiScopeHelper soapScopeHelper;
    private final JsonRpcApiScopeHelper jsonRpcScopeHelper;
    private final Iterable<ApiResourceInfo> apiResourceInfo;
    private final RestApiScopeHelper restApiScopeHelper;

    protected JiraScope(String key, Collection<String> methods, Collection<RestApiScopeHelper.RestScope> resources)
    {
        super(key, ImmutableSet.of(InstallationMode.LOCAL, REMOTE));
        this.soapScopeHelper = new RpcEncodedSoapApiScopeHelper("/rpc/soap/jirasoapservice-v2", "http://soap.rpc.jira.atlassian.com", checkNotNull(methods));
        this.jsonRpcScopeHelper = new JsonRpcApiScopeHelper("/rpc/json-rpc/jirasoapservice-v2", methods);
        this.restApiScopeHelper = new RestApiScopeHelper(checkNotNull(resources));
        this.apiResourceInfo = concat(soapScopeHelper.getApiResourceInfos(), jsonRpcScopeHelper.getApiResourceInfos(), restApiScopeHelper.getApiResourceInfos());
    }

    @Override
    public final boolean allow(HttpServletRequest request, String user)
    {
        return soapScopeHelper.allow(request, user) || jsonRpcScopeHelper.allow(request, user) || restApiScopeHelper.allow(request, user);
    }

    @Override
    public final Iterable<ApiResourceInfo> getApiResourceInfos()
    {
        return apiResourceInfo;
    }
}
