package com.atlassian.plugin.connect.plugin.scopes;

import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.spi.scope.AddOnScope;
import com.atlassian.plugin.connect.spi.scope.helper.AddOnScopeLoadJsonFileHelper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.google.common.collect.Collections2.transform;

public class StaticAddOnScopes
{
    @VisibleForTesting
    public static Collection<AddOnScope> buildFor(URL... scopeFileUrls) throws IOException
    {
        Map<ScopeName, AddOnScope> keyToScope = new HashMap<>();

        for (URL scopeFileUrl : scopeFileUrls)
        {
            AddOnScopeLoadJsonFileHelper.addProductScopesFromFile(keyToScope, scopeFileUrl);
        }

        // copy element references into an ArrayList so that equals() comparisons work
        // sort to protect against ordering throwing off ArrayList.equals() and to make toString() look nicer
        ArrayList<AddOnScope> addOnScopes = new ArrayList<>(keyToScope.values());
        Collections.sort(addOnScopes);
        return addOnScopes;
    }

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
