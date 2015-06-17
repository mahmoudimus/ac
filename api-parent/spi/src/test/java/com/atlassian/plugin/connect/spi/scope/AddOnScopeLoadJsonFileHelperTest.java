package com.atlassian.plugin.connect.spi.scope;

import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.spi.scope.helper.PathScopeHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import static com.atlassian.plugin.connect.spi.scope.helper.AddOnScopeLoadJsonFileHelper.combineProductScopes;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AddOnScopeLoadJsonFileHelperTest
{
    @Test
    public void testCombineSeparate() throws Exception
    {
        ImmutableList<ScopeName> scopeNamesSource = ImmutableList.of(ScopeName.READ, ScopeName.WRITE);
        ImmutableList<ScopeName> scopeNamesAdding = ImmutableList.of(ScopeName.ADMIN, ScopeName.PROJECT_ADMIN);

        Map<ScopeName, AddOnScope> source = mapWithEmptyScopeFromScopeNames(scopeNamesSource);
        combineProductScopes(
                source,
                mapWithEmptyScopeFromScopeNames(scopeNamesAdding));

        final Matcher<Iterable<?>> expectedMatcher = Matchers.containsInAnyOrder(ImmutableSet.builder().addAll(scopeNamesAdding).addAll(scopeNamesSource).build().toArray());
        assertThat(source.keySet(), expectedMatcher);
    }

    @Test
    public void testCombineSameKeys() throws Exception
    {
        Map<ScopeName, AddOnScope> source = exampleScopeMapWithPath(ScopeName.READ, "a");
        combineProductScopes(
                source,
                exampleScopeMapWithPath(ScopeName.READ, "b"));

        AddOnScope resultingScope = source.get(ScopeName.READ);

        assertThat(resultingScope, allowsAccessTo("/a"));
        assertThat(resultingScope, allowsAccessTo("/b"));
    }

    private Map<ScopeName, AddOnScope> exampleScopeMapWithPath(final ScopeName scopeName, final String key)
    {
        Map<ScopeName, AddOnScope> map = new HashMap<>();

        PathScopeHelper pathScope = new PathScopeHelper(false, "/" + key);
        AddOnScopeApiPath apiPath = new AddOnScopeApiPath.ApiPath(Arrays.asList(pathScope));

        map.put(scopeName, new AddOnScope("none", Arrays.asList(apiPath)));

        return map;
    }

    private Map<ScopeName, AddOnScope> mapWithEmptyScopeFromScopeNames(Iterable<ScopeName> scopeNames)
    {
        Map<ScopeName, AddOnScope> map = new HashMap<>();
        for (ScopeName scopeName : scopeNames)
        {
            map.put(scopeName, new AddOnScope("none", Collections.<AddOnScopeApiPath>emptyList()));
        }
        return map;
    }

    private Matcher<? super AddOnScope> allowsAccessTo(final String url)
    {
        return new TypeSafeMatcher<AddOnScope>()
        {
            @Override
            protected boolean matchesSafely(final AddOnScope addOnScope)
            {
                HttpServletRequest request = mock(HttpServletRequest.class);

                when(request.getRequestURI()).thenReturn(url);
                when(request.getContextPath()).thenReturn("");
                when(request.getMethod()).thenReturn("GET");
                return addOnScope.allow(request, null);
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("allowed access to " + url);
            }
        };
    }
}
