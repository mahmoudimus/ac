package com.atlassian.plugin.connect.plugin.scopes;

import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;

import java.util.HashSet;
import java.util.Set;

public class NormalizedScopeSet extends HashSet<ScopeName>
{
    public NormalizedScopeSet(Set<ScopeName> scopes)
    {
        for (ScopeName scopeName : scopes)
        {
            add(scopeName);
            addAll(scopeName.getImplied());
        }
    }
}
