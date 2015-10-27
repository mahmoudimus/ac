package com.atlassian.plugin.connect.spi.scope;

import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;

import java.util.Map;

public interface ProductScopeProvider
{
    /**
     * Get a Map of scopes used to whitelist incoming {@link javax.servlet.http.HttpServletRequest}s.
     * This method is called during plugin installation.
     *
     * @return the {@link Map} of {@link ScopeName}s on {@link AddOnScope}s used to whitelist incoming {@link javax.servlet.http.HttpServletRequest}s
     */
    Map<ScopeName, AddOnScope> getScopes();
}
