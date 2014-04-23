package com.atlassian.plugin.connect.modules.beans.nested;

import com.google.common.base.Predicate;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.collect.Collections2.filter;
import static java.util.Arrays.asList;

public enum ScopeName implements Comparable<ScopeName>
{
    /**
     * Note: PROJECT_ADMIN and SPACE_ADMIN are effectively synonyms that prevent
     * the word "space" from appearing in JIRA configs and "project" in the Confluence configs.
     * AGENT represents the ability to act on behalf of users who have authorised such agency.
     */
    READ, WRITE, DELETE, PROJECT_ADMIN, SPACE_ADMIN, ADMIN, AGENT;

    public boolean implies(ScopeName other)
    {
        return AGENT != this && ordinal() > other.ordinal() && !(equals(SPACE_ADMIN) && other.equals(PROJECT_ADMIN));
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

}
