package com.atlassian.plugin.connect.plugin.scopes;

import com.google.common.collect.Lists;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.Collection;
import java.util.Iterator;

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
        Collection actualsToCheck = Lists.newArrayList(actuals);

        while (scopeMatchersIter.hasNext())
        {
            Matcher<AddOnScope> scopeMatcher = scopeMatchersIter.next();
            boolean matched = false;

            Iterator<Object> actualsIter = actualsToCheck.iterator();
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

        return actualsToCheck.isEmpty();
    }

    @Override
    public void describeTo(Description description)
    {
        description.appendList(" <[", ",", "]>", scopeMatchers);
    }
}
