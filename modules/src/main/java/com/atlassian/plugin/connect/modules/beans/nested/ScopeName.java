package com.atlassian.plugin.connect.modules.beans.nested;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.collect.Collections2.filter;
import static java.util.Arrays.asList;

public enum ScopeName implements Comparable<ScopeName>
{
    /**
     * Note: PROJECT_ADMIN and SPACE_ADMIN are effectively synonyms that prevent
     * the word "space" from appearing in JIRA configs and "project" in Confluence configs.
     */
    READ, WRITE, DELETE, PROJECT_ADMIN, SPACE_ADMIN, ADMIN;

    private boolean implies(ScopeName other)
    {
        return ordinal() > other.ordinal() && !(equals(SPACE_ADMIN) && other.equals(PROJECT_ADMIN));
    }

    public Set<ScopeName> getImplied()
    {
        return new HashSet<ScopeName>(filter(asList(values()), new Predicate<ScopeName>()
        {
            @Override
            public boolean apply(@Nullable ScopeName scopeName)
            {
                return null != scopeName && implies(scopeName);
            }
        }));
    }

    /**
     * Turn a {@link Set} of {@link ScopeName}s into itself plus every implied {@link ScopeName}.
     * @param scopes arbitrary {@link ScopeName}s
     * @return new {@link Set} containing the original {@link ScopeName}s plus their implied {@link ScopeName}s
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
     * Find the topmost {@link ScopeName} in a {@link Set} of {@link ScopeName}s. The topmost
     * scope is the one which implies all others in the set.
     * @param scopes arbitrary {@link ScopeName}s
     * @return the {@link ScopeName} which implies all others in the {@link Set}.
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
        return isTransitionTo(ADMIN, previousScopes, newScopes);
    }

    public static boolean isTransitionDownFromAdmin(Set<ScopeName> previousScopes, Set<ScopeName> newScopes)
    {
        return isTransitionAwayFrom(ADMIN, previousScopes, newScopes);
    }

    public static boolean isTransitionUpToProjectAdmin(Set<ScopeName> previousScopes, Set<ScopeName> newScopes)
    {
        return isTransitionTo(PROJECT_ADMIN, previousScopes, newScopes);
    }

    public static boolean isTransitionDownFromProjectAdmin(Set<ScopeName> previousScopes, Set<ScopeName> newScopes)
    {
        return isTransitionAwayFrom(PROJECT_ADMIN, previousScopes, newScopes);
    }

    // For now we are giving addons that have at least WRITE scope (but not ADMIN) SPACE_ADMIN permissions
    public static boolean isTransitionDownFromNonTopAdminToRead(Set<ScopeName> previousScopes, Set<ScopeName> newScopes)
    {
        return hasGreaterThanReadAndLessThanTopAdmin(previousScopes) && hasReadOrLess(newScopes);
    }

    public static boolean isTransitionUpFromReadToNonTopAdmin(Set<ScopeName> previousScopes, Set<ScopeName> newScopes)
    {
        return hasGreaterThanReadAndLessThanTopAdmin(newScopes) && hasReadOrLess(previousScopes);
    }

    private static boolean hasGreaterThanReadAndLessThanTopAdmin(Set<ScopeName> scopeNames)
    {
        ScopeName topMost = findTopMostScope(scopeNames);
        return topMost == WRITE || topMost == DELETE || topMost == SPACE_ADMIN;
    }

    private static boolean hasReadOrLess(Set<ScopeName> scopeNames)
    {
        ScopeName topMost = findTopMostScope(scopeNames);
        return topMost == null || topMost == READ;
    }

    public static boolean isTransitionUpToSpaceAdmin(Set<ScopeName> previousScopes, Set<ScopeName> newScopes)
    {
        return isTransitionTo(SPACE_ADMIN, previousScopes, newScopes);
    }

    public static boolean isTransitionDownFromSpaceAdmin(Set<ScopeName> previousScopes, Set<ScopeName> newScopes)
    {
        return isTransitionAwayFrom(SPACE_ADMIN, previousScopes, newScopes);
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
