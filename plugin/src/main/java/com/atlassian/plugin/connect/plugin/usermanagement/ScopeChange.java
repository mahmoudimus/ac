package com.atlassian.plugin.connect.plugin.usermanagement;

import java.util.Set;

import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.google.common.collect.Sets;

import static com.google.common.base.Preconditions.checkNotNull;

public class ScopeChange
{
    private final Set<ScopeName> removedScopes;
    private final Set<ScopeName> addedScopes;

    public ScopeChange(Set<ScopeName> previousScopes, Set<ScopeName> newScopes)
    {
        Set<ScopeName> prev = ScopeName.normalize(checkNotNull(previousScopes));
        Set<ScopeName> nuevo = ScopeName.normalize(checkNotNull(newScopes));
        removedScopes = Sets.difference(prev, nuevo);
        addedScopes = Sets.difference(nuevo, prev);
    }

    public Set<ScopeName> getRemovedScopes()
    {
        return removedScopes;
    }

    public Set<ScopeName> getAddedScopes()
    {
        return addedScopes;
    }
}
