package com.atlassian.plugin.connect.modules.beans.nested;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

import javax.annotation.Nullable;

import java.lang.Comparable;
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
}
