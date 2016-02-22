package com.atlassian.plugin.connect.plugin.auth.scope;

import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.auth.scope.whitelist.AddonScope;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Collections2.transform;

public class StaticAddonScopes {

    /**
     * Turn lightweight references to scopes into the scopes themselves.
     *
     * @param scopes    {@link AddonScope addon scopes} previously read from static configuration
     * @param scopeKeys lightweight references to scopes
     * @return the {@link AddonScope addon scopes} referenced by the {@link String strings}
     * @throws IllegalArgumentException if any of the scopeKeys do not appear amongst the static scopes
     */
    public static Collection<AddonScope> dereference(Collection<AddonScope> scopes, @Nonnull final Collection<ScopeName> scopeKeys) {
        // avoid loading scopes from file if unnecessary
        if (scopeKeys.isEmpty()) {
            return Collections.emptySet();
        }

        final Map<ScopeName, AddonScope> scopeKeyToScope = new HashMap<>(scopes.size());

        for (AddonScope scope : scopes) {
            ScopeName scopeName = ScopeName.valueOf(scope.getKey());

            if (scopeKeyToScope.containsKey(scopeName)) {
                throw new IllegalArgumentException(String.format("Scope name '%s' is specified multiple times.", scope.getKey()));
            }

            scopeKeyToScope.put(scopeName, scope);
        }

        if (!scopeKeyToScope.keySet().containsAll(scopeKeys)) {
            Set<ScopeName> badKeys = new HashSet<>(scopeKeys);
            badKeys.removeAll(scopeKeyToScope.keySet());
            throw new IllegalArgumentException(String.format("Scope keys %s do not exist. Valid values are: %s.", badKeys, scopeKeyToScope.keySet()));
        }

        return transform(addImpliedScopesTo(scopeKeys), scopeKey -> {
            if (null == scopeKey) return null;
            return scopeKeyToScope.get(scopeKey);
        });
    }

    private static Collection<ScopeName> addImpliedScopesTo(Collection<ScopeName> scopeReferences) {
        Set<ScopeName> allScopeReferences = new HashSet<>(scopeReferences);

        for (ScopeName scopeReference : scopeReferences) {
            allScopeReferences.addAll(scopeReference.getImplied());
        }

        return allScopeReferences;
    }
}
