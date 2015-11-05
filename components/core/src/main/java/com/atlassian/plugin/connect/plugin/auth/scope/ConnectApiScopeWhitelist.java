package com.atlassian.plugin.connect.plugin.auth.scope;

import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.spi.scope.AddOnScope;

import java.util.Map;

public class ConnectApiScopeWhitelist
{

    private Map<ScopeName, AddOnScope> scopes;

    public ConnectApiScopeWhitelist(Map<ScopeName, AddOnScope> scopes)
    {
        this.scopes = scopes;
    }

    public Map<ScopeName, AddOnScope> getScopes()
    {
        return scopes;
    }
}
