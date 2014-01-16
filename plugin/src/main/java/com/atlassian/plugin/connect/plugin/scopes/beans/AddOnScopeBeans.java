package com.atlassian.plugin.connect.plugin.scopes.beans;

import java.util.Collection;

public class AddOnScopeBeans
{
    private Collection<AddOnScopeBean> scopes; // set by gson
    private Collection<AddOnScopeBean.RestPathBean> restPaths; // set by gson

    public Collection<AddOnScopeBean> getScopes()
    {
        return scopes;
    }

    public Collection<AddOnScopeBean.RestPathBean> getRestPaths()
    {
        return restPaths;
    }
}
