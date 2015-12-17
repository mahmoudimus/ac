package com.atlassian.plugin.connect.plugin.auth.scope.whitelist;

import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;

import java.util.Map;

public class ConnectApiScopeWhitelist
{

    private Map<ScopeName, AddonScope> scopes;

    public ConnectApiScopeWhitelist(Map<ScopeName, AddonScope> scopes)
    {
        this.scopes = scopes;
    }

    public Map<ScopeName, AddonScope> getScopes()
    {
        return scopes;
    }
}
