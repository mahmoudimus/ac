package com.atlassian.plugin.connect.modules.beans.nested;

import com.google.common.base.Predicate;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.collect.Collections2.filter;
import static java.util.Arrays.asList;

public enum ScopeName
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
     * Returns an integer indicating the canonical display order for scopes. This is
     * currently the same value returned by <tt>ordinal()</tt>.
     */
    public int getSortOrder()
    {
        return ordinal();
    }

    /**
     * Returns the name of an AUI icon associated with this scope.
     */
    public String getIconKey()
    {
        switch (this)
        {
            case READ:
                return "view";
            case WRITE:
                return "edit";
            case DELETE:
                return "remove";
            default:
                return "user";
        }
    }
}
