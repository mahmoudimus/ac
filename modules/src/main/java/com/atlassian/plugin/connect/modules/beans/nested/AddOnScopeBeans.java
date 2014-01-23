package com.atlassian.plugin.connect.modules.beans.nested;

import java.util.Collection;

public class AddOnScopeBeans
{
    private Collection<AddOnScopeBean> scopes; // set by gson
    private Collection<AddOnScopeBean.RestPathBean> restPaths; // set by gson
    private Collection<AddOnScopeBean.SoapRpcPathBean> soapRpcPaths; // set by gson

    public AddOnScopeBeans()
    {
        this(null, null, null);
    }

    public AddOnScopeBeans(Collection<AddOnScopeBean> scopes, Collection<AddOnScopeBean.RestPathBean> restPaths, Collection<AddOnScopeBean.SoapRpcPathBean> soapRpcPaths)
    {
        this.scopes = scopes;
        this.restPaths = restPaths;
        this.soapRpcPaths = soapRpcPaths;
    }

    public Collection<AddOnScopeBean> getScopes()
    {
        return scopes;
    }

    public Collection<AddOnScopeBean.RestPathBean> getRestPaths()
    {
        return restPaths;
    }

    public Collection<AddOnScopeBean.SoapRpcPathBean> getSoapRpcPaths()
    {
        return soapRpcPaths;
    }
}
