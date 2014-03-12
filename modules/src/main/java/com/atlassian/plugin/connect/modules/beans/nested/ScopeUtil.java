package com.atlassian.plugin.connect.modules.beans.nested;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;

public class ScopeUtil
{
    /**
     * Turn a {@link java.util.Set} of {@link com.atlassian.plugin.connect.modules.beans.nested.ScopeName}s into itself plus every implied {@link com.atlassian.plugin.connect.modules.beans.nested.ScopeName}.
     * @param scopes arbitrary {@link com.atlassian.plugin.connect.modules.beans.nested.ScopeName}s
     * @return new {@link java.util.Set} containing the original {@link com.atlassian.plugin.connect.modules.beans.nested.ScopeName}s plus their implied {@link com.atlassian.plugin.connect.modules.beans.nested.ScopeName}s
     */
    public static Set<ScopeName> normalize(Set<ScopeName> scopes)
    {
        HashSet<ScopeName> normalizedScopes = Sets.newHashSet();

        if (null != scopes)
        {
            for (ScopeName scopeName : scopes)
            {
                normalizedScopes.add(scopeName);
                normalizedScopes.addAll(scopeName.getImplied());
            }
        }

        return normalizedScopes;
    }

    /**
     * Find the topmost {@link com.atlassian.plugin.connect.modules.beans.nested.ScopeName} in a {@link java.util.Set} of {@link com.atlassian.plugin.connect.modules.beans.nested.ScopeName}s. The topmost
     * scope is the one which implies all others in the set.
     * @param scopes arbitrary {@link com.atlassian.plugin.connect.modules.beans.nested.ScopeName}s
     * @return the {@link com.atlassian.plugin.connect.modules.beans.nested.ScopeName} which implies all others in the {@link java.util.Set}.
     */
    public static ScopeName findTopMostScope(Set<ScopeName> scopes)
    {
        ScopeName topMostScope = null;
        for (ScopeName scope : scopes)
        {
            if (topMostScope == null || scope.implies(topMostScope))
            {
                topMostScope = scope;
            }
        }
        return topMostScope;
    }

    public static boolean isTransitionUpToAdmin(Set<ScopeName> previousScopes, Set<ScopeName> newScopes)
    {
        return isTransitionTo(ScopeName.ADMIN, previousScopes, newScopes);
    }

    public static boolean isTransitionDownFromAdmin(Set<ScopeName> previousScopes, Set<ScopeName> newScopes)
    {
        return isTransitionAwayFrom(ScopeName.ADMIN, previousScopes, newScopes);
    }

    public static boolean isTransitionUpToProjectAdmin(Set<ScopeName> previousScopes, Set<ScopeName> newScopes)
    {
        return isTransitionTo(ScopeName.PROJECT_ADMIN, previousScopes, newScopes);
    }

    public static boolean isTransitionDownFromProjectAdmin(Set<ScopeName> previousScopes, Set<ScopeName> newScopes)
    {
        return isTransitionAwayFrom(ScopeName.PROJECT_ADMIN, previousScopes, newScopes);
    }

    // For now we are giving addons that have at least WRITE scope SPACE_ADMIN permissions
    public static boolean isTransitionDownToReadOrLess(Set<ScopeName> previousScopes, Set<ScopeName> newScopes)
    {
        return !hasReadOrLess(previousScopes) && hasReadOrLess(newScopes);
    }

    public static boolean isTransitionUpFromReadOrLess(Set<ScopeName> previousScopes, Set<ScopeName> newScopes)
    {
        return !hasReadOrLess(newScopes) && hasReadOrLess(previousScopes);
    }

    private static boolean hasReadOrLess(Set<ScopeName> scopeNames)
    {
        ScopeName topMost = findTopMostScope(scopeNames);
        return topMost == null || topMost == ScopeName.READ;
    }

    public static boolean isTransitionUpToSpaceAdmin(Set<ScopeName> previousScopes, Set<ScopeName> newScopes)
    {
        return isTransitionTo(ScopeName.SPACE_ADMIN, previousScopes, newScopes);
    }

    public static boolean isTransitionDownFromSpaceAdmin(Set<ScopeName> previousScopes, Set<ScopeName> newScopes)
    {
        return isTransitionAwayFrom(ScopeName.SPACE_ADMIN, previousScopes, newScopes);
    }

    public static boolean isTransitionDownToRead(Set<ScopeName> previousScopes, Set<ScopeName> newScopes)
    {
        return isTransitionTo(ScopeName.READ, previousScopes, newScopes);
    }

    private static boolean isTransitionTo(ScopeName scopeName, Set<ScopeName> previousScopes, Set<ScopeName> newScopes)
    {
        ScopeName previousTopMost = findTopMostScope(previousScopes);
        ScopeName newTopMost = findTopMostScope(newScopes);
        return scopeName.equals(newTopMost) && !scopeName.equals(previousTopMost);
    }

    private static boolean isTransitionAwayFrom(ScopeName scopeName, Set<ScopeName> previousScopes, Set<ScopeName> newScopes)
    {
        // away is the reverse of to: see the arg order
        return isTransitionTo(scopeName, newScopes, previousScopes);
    }
}
