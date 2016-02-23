package com.atlassian.plugin.connect.plugin.auth.scope;

import com.atlassian.plugin.connect.plugin.auth.scope.whitelist.AddonScope;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.Collection;
import java.util.Iterator;

import static com.google.common.base.Preconditions.checkNotNull;

public class AddonScopesMatcher extends TypeSafeMatcher<Collection<AddonScope>> {
    private final Collection<Matcher<AddonScope>> scopeMatchers;

    public AddonScopesMatcher(Collection<Matcher<AddonScope>> scopeMatchers) {
        this.scopeMatchers = checkNotNull(scopeMatchers);
    }

    @Override
    protected boolean matchesSafely(final Collection<AddonScope> addonScopes) {
        // shortcut for efficiency
        if (addonScopes.size() != scopeMatchers.size()) {
            return false;
        }

        for (final Matcher<AddonScope> scopeMatcher : scopeMatchers) {
            boolean matched = false;

            Iterator<AddonScope> actualsIter = addonScopes.iterator();
            while (actualsIter.hasNext() && !matched) {
                if (scopeMatcher.matches(actualsIter.next())) {
                    actualsIter.remove();
                    matched = true;
                }
            }
            if (!matched) {
                return false;
            }
        }

        return addonScopes.isEmpty();
    }

    @Override
    public void describeTo(Description description) {
        description.appendList(" <[", ",", "]>", scopeMatchers);
    }
}
