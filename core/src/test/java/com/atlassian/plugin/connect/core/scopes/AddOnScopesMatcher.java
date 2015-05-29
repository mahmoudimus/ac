package com.atlassian.plugin.connect.core.scopes;

import com.atlassian.plugin.connect.spi.scope.AddOnScope;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.Collection;
import java.util.Iterator;

import static com.google.common.base.Preconditions.checkNotNull;

public class AddOnScopesMatcher extends TypeSafeMatcher<Collection<AddOnScope>>
{
    private final Collection<Matcher<AddOnScope>> scopeMatchers;

    public AddOnScopesMatcher(Collection<Matcher<AddOnScope>> scopeMatchers)
    {
        this.scopeMatchers = checkNotNull(scopeMatchers);
    }

    @Override
    protected boolean matchesSafely(final Collection<AddOnScope> addOnScopes)
    {
        // shortcut for efficiency
        if (addOnScopes.size() != scopeMatchers.size())
        {
            return false;
        }

        for (final Matcher<AddOnScope> scopeMatcher : scopeMatchers)
        {
            boolean matched = false;

            Iterator<AddOnScope> actualsIter = addOnScopes.iterator();
            while (actualsIter.hasNext() && !matched)
            {
                if (scopeMatcher.matches(actualsIter.next()))
                {
                    actualsIter.remove();
                    matched = true;
                }
            }
            if (!matched)
            {
                return false;
            }
        }

        return addOnScopes.isEmpty();
    }

    @Override
    public void describeTo(Description description)
    {
        description.appendList(" <[", ",", "]>", scopeMatchers);
    }
}
