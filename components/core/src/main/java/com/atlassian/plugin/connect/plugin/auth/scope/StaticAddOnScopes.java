package com.atlassian.plugin.connect.plugin.auth.scope;

import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.auth.scope.whitelist.AddOnScope;
import com.google.common.base.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Collections2.transform;

public class StaticAddOnScopes
{

    /**
     * Turn lightweight references to scopes into the scopes themselves.
     *
     * @param scopes    {@link AddOnScope}s previously read from static configuration
     * @param scopeKeys lightweight references to scopes
     * @return the {@link AddOnScope}s referenced by the {@link String}s
     * @throws IllegalArgumentException if any of the scopeKeys do not appear amongst the static scopes
     */
    public static Collection<AddOnScope> dereference(Collection<AddOnScope> scopes, @Nonnull final Collection<ScopeName> scopeKeys)
    {
        // avoid loading scopes from file if unnecessary
        if (scopeKeys.isEmpty())
        {
            return Collections.emptySet();
        }

        final Map<ScopeName, AddOnScope> scopeKeyToScope = new HashMap<>(scopes.size());

        for (AddOnScope scope : scopes)
        {
            ScopeName scopeName = ScopeName.valueOf(scope.getKey());

            if (scopeKeyToScope.containsKey(scopeName))
            {
                throw new IllegalArgumentException(String.format("Scope name '%s' is specified multiple times.", scope.getKey()));
            }

            scopeKeyToScope.put(scopeName, scope);
        }

        if (!scopeKeyToScope.keySet().containsAll(scopeKeys))
        {
            Set<ScopeName> badKeys = new HashSet<>(scopeKeys);
            badKeys.removeAll(scopeKeyToScope.keySet());
            throw new IllegalArgumentException(String.format("Scope keys %s do not exist. Valid values are: %s.", badKeys, scopeKeyToScope.keySet()));
        }

        return transform(addImpliedScopesTo(scopeKeys), new Function<ScopeName, AddOnScope>()
        {
            @Override
            public AddOnScope apply(@Nullable ScopeName scopeKey)
            {
                if (null == scopeKey)
                {
                    return null;
                }

                return scopeKeyToScope.get(scopeKey);
            }
        });
    }

    private static Collection<ScopeName> addImpliedScopesTo(Collection<ScopeName> scopeReferences)
    {
        Set<ScopeName> allScopeReferences = new HashSet<>(scopeReferences);

        for (ScopeName scopeReference : scopeReferences)
        {
            allScopeReferences.addAll(scopeReference.getImplied());
        }

        return allScopeReferences;
    }
}
