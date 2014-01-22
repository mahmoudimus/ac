package com.atlassian.plugin.connect.test.plugin.scopes;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.Collection;
import java.util.Iterator;

import com.atlassian.plugin.connect.plugin.scopes.AddOnScope;

import static com.google.common.base.Preconditions.checkNotNull;

public class AddOnScopesMatcher extends BaseMatcher<Collection<AddOnScope>>
{
    private final Collection<Matcher<AddOnScope>> scopeMatchers;

    public AddOnScopesMatcher(Collection<Matcher<AddOnScope>> scopeMatchers)
    {
        this.scopeMatchers = checkNotNull(scopeMatchers);
    }

    @Override
    public boolean matches(Object item)
    {
        if (!(item instanceof Collection))
        {
            System.out.println("Not a Collection: " + item);
            return false;
        }

        Collection actuals = (Collection) item;

        // shortcut for efficiency
        if (actuals.size() != scopeMatchers.size())
        {
            return false;
        }

        Iterator<Matcher<AddOnScope>> scopeMatchersIter = scopeMatchers.iterator();
        Iterator<Object> actualsIter = actuals.iterator();

        while (scopeMatchersIter.hasNext() && actualsIter.hasNext())
        {
            if (!scopeMatchersIter.next().matches(actualsIter.next()))
            {
                return false;
            }
        }

        return !scopeMatchersIter.hasNext() && !actualsIter.hasNext();
    }

    @Override
    public void describeTo(Description description)
    {
        description.appendList(" <[", ",", "]>", scopeMatchers);
    }
}
