package com.atlassian.plugin.connect.plugin.auth.scope;

import com.atlassian.plugin.connect.plugin.auth.scope.whitelist.AddonScope;

import java.util.Collection;

public interface ScopeService {
    /**
     * Build the scopes for the current product.
     *
     * @return The scopes for the current product.
     */
    Collection<AddonScope> build();
}
