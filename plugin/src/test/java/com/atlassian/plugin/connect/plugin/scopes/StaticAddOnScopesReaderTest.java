package com.atlassian.plugin.connect.plugin.scopes;

import com.atlassian.plugin.connect.spi.permission.scope.RestApiScopeHelper;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class StaticAddOnScopesReaderTest
{
    @Test
    public void readsConfluenceScopes() throws IOException
    {
        assertThat(StaticAddOnScopesReader.buildFor("test"), is(expected()));
    }

    private Matcher<Collection<AddOnScope>> expected()
    {
        AddOnScopeApiPath path = new AddOnScopeApiPath.RestApiPath(asList(
                new RestApiScopeHelper.RestScope("mywork", asList("1", "latest"), "/notification", asList("get")),
                new RestApiScopeHelper.RestScope("mywork", asList("1", "latest"), "/status", asList("get")),
                new RestApiScopeHelper.RestScope("mywork", asList("1", "latest"), "/task", asList("get")),
                new RestApiScopeHelper.RestScope("prototype", asList("1", "latest"), "/search", asList("get")),
                new RestApiScopeHelper.RestScope("prototype", asList("1", "latest"), "/content", asList("get")),
                new RestApiScopeHelper.RestScope("prototype", asList("1", "latest"), "/attachment", asList("get")),
                new RestApiScopeHelper.RestScope("prototype", asList("1", "latest"), "/breadcrumb", asList("get")),
                new RestApiScopeHelper.RestScope("prototype", asList("1", "latest"), "/space", asList("get")),
                new RestApiScopeHelper.RestScope("ui", asList("1", "1.0", "latest"), "/content", asList("get"))
            ));
        Matcher<AddOnScope> readScope = new ScopeMatcher("READ", asList(path));
        return new ScopesMatcher(asList(readScope));
    }

    private static class ScopesMatcher extends BaseMatcher<Collection<AddOnScope>>
    {
        private final Collection<Matcher<AddOnScope>> scopeMatchers;

        private ScopesMatcher(Collection<Matcher<AddOnScope>> scopeMatchers)
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

    private static class ScopeMatcher extends BaseMatcher<AddOnScope>
    {
        private final String key;
        private final Collection<AddOnScopeApiPath> paths;

        public ScopeMatcher(String key, Collection<AddOnScopeApiPath> paths)
        {
            this.key = checkNotNull(key);
            this.paths = checkNotNull(paths);
        }

        @Override
        public boolean matches(Object item)
        {
            if (!(item instanceof AddOnScope))
            {
                System.out.println("Not an AddOnScope: " + item);
                return false;
            }

            AddOnScope scope = (AddOnScope) item;

            if (!key.equals(scope.getKey()))
            {
                System.out.println(key + " != " + scope.getKey());
            }

            if (!paths.equals(scope.getPaths()))
            {
                System.out.println(paths.toString() + " != " + scope.getPaths());
            }

            return key.equals(scope.getKey()) && paths.equals(scope.getPaths());
        }

        @Override
        public void describeTo(Description description)
        {
            description.appendText(AddOnScope.class.getSimpleName());
            description.appendText("[key=");
            description.appendText(key);
            description.appendText(",paths=");
            description.appendText(paths.toString());
            description.appendText("]");
        }
    }
}
