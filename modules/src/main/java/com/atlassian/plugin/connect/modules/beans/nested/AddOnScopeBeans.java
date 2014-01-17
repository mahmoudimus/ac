package com.atlassian.plugin.connect.modules.beans.nested;

import java.util.Collection;

public class AddOnScopeBeans
{
    private Collection<AddOnScopeBean> scopes; // set by gson
    private Collection<AddOnScopeBean.RestPathBean> restPaths; // set by gson

    public AddOnScopeBeans()
    {
        this(null, null);
    }

    public AddOnScopeBeans(Collection<AddOnScopeBean> scopes, Collection<AddOnScopeBean.RestPathBean> restPaths)
    {
        this.scopes = scopes;
        this.restPaths = restPaths;
    }

    public Collection<AddOnScopeBean> getScopes()
    {
        return scopes;
    }

    public Collection<AddOnScopeBean.RestPathBean> getRestPaths()
    {
        return restPaths;
    }
}
