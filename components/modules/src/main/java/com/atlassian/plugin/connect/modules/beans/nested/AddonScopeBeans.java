package com.atlassian.plugin.connect.modules.beans.nested;

import java.util.Collection;

public class AddonScopeBeans {
    private Collection<AddonScopeBean> scopes; // set by gson
    private Collection<AddonScopeBean.RestPathBean> restPaths; // set by gson
    private Collection<AddonScopeBean.SoapRpcPathBean> soapRpcPaths; // set by gson
    private Collection<AddonScopeBean.JsonRpcPathBean> jsonRpcPaths; // set by gson
    private Collection<AddonScopeBean.XmlRpcBean> xmlRpcPaths; // set by gson
    private Collection<AddonScopeBean.PathBean> paths; // set by gson

    public AddonScopeBeans() {
        this(null, null, null, null, null, null);
    }

    public AddonScopeBeans(Collection<AddonScopeBean> scopes,
                           Collection<AddonScopeBean.RestPathBean> restPaths,
                           Collection<AddonScopeBean.SoapRpcPathBean> soapRpcPaths,
                           Collection<AddonScopeBean.JsonRpcPathBean> jsonRpcPaths,
                           Collection<AddonScopeBean.XmlRpcBean> xmlRpcPaths,
                           Collection<AddonScopeBean.PathBean> paths) {
        this.scopes = scopes;
        this.restPaths = restPaths;
        this.soapRpcPaths = soapRpcPaths;
        this.jsonRpcPaths = jsonRpcPaths;
        this.xmlRpcPaths = xmlRpcPaths;
        this.paths = paths;
    }

    public Collection<AddonScopeBean> getScopes() {
        return scopes;
    }

    public Collection<AddonScopeBean.RestPathBean> getRestPaths() {
        return restPaths;
    }

    public Collection<AddonScopeBean.SoapRpcPathBean> getSoapRpcPaths() {
        return soapRpcPaths;
    }

    public Collection<AddonScopeBean.JsonRpcPathBean> getJsonRpcPaths() {
        return jsonRpcPaths;
    }

    public Collection<AddonScopeBean.XmlRpcBean> getXmlRpcPaths() {
        return xmlRpcPaths;
    }

    public Collection<AddonScopeBean.PathBean> getPaths() {
        return paths;
    }
}
