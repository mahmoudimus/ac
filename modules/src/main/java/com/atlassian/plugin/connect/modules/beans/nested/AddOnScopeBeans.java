package com.atlassian.plugin.connect.modules.beans.nested;

import java.util.Collection;

public class AddOnScopeBeans
{
    private Collection<AddOnScopeBean> scopes; // set by gson
    private Collection<AddOnScopeBean.RestPathBean> restPaths; // set by gson
    private Collection<AddOnScopeBean.SoapRpcPathBean> soapRpcPaths; // set by gson
    private Collection<AddOnScopeBean.JsonRpcPathBean> jsonRpcPaths; // set by gson
    private Collection<AddOnScopeBean.XmlRpcBean> xmlRpcPaths; // set by gson
    private Collection<AddOnScopeBean.PathBean> paths; // set by gson

    public AddOnScopeBeans()
    {
        this(null, null, null, null, null, null);
    }

    public AddOnScopeBeans(Collection<AddOnScopeBean> scopes,
                           Collection<AddOnScopeBean.RestPathBean> restPaths,
                           Collection<AddOnScopeBean.SoapRpcPathBean> soapRpcPaths,
                           Collection<AddOnScopeBean.JsonRpcPathBean> jsonRpcPaths,
                           Collection<AddOnScopeBean.XmlRpcBean> xmlRpcPaths,
                           Collection<AddOnScopeBean.PathBean> paths)
    {
        this.scopes = scopes;
        this.restPaths = restPaths;
        this.soapRpcPaths = soapRpcPaths;
        this.jsonRpcPaths = jsonRpcPaths;
        this.xmlRpcPaths = xmlRpcPaths;
        this.paths = paths;
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

    public Collection<AddOnScopeBean.JsonRpcPathBean> getJsonRpcPaths()
    {
        return jsonRpcPaths;
    }

    public Collection<AddOnScopeBean.XmlRpcBean> getXmlRpcPaths()
    {
        return xmlRpcPaths;
    }

    public Collection<AddOnScopeBean.PathBean> getPaths()
    {
        return paths;
    }
}
