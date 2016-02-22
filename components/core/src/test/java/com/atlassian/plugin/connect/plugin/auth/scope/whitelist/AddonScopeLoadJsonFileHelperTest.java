package com.atlassian.plugin.connect.plugin.auth.scope.whitelist;

import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.atlassian.plugin.connect.plugin.auth.scope.whitelist.AddonScopeLoadJsonFileHelper.combineScopes;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AddonScopeLoadJsonFileHelperTest
{
    @Test
    public void testCombineSeparate() throws Exception
    {
        ImmutableList<ScopeName> scopeNamesSource = ImmutableList.of(ScopeName.READ, ScopeName.WRITE);
        ImmutableList<ScopeName> scopeNamesAdding = ImmutableList.of(ScopeName.ADMIN, ScopeName.PROJECT_ADMIN);

        Map<ScopeName, AddonScope> source = mapWithEmptyScopeFromScopeNames(scopeNamesSource);
        combineScopes(
                source,
                mapWithEmptyScopeFromScopeNames(scopeNamesAdding));

        final Matcher<Iterable<?>> expectedMatcher = Matchers.containsInAnyOrder(ImmutableSet.builder().addAll(scopeNamesAdding).addAll(scopeNamesSource).build().toArray());
        assertThat(source.keySet(), expectedMatcher);
    }

    @Test
    public void testCombineSameKeys() throws Exception
    {
        Map<ScopeName, AddonScope> source = exampleScopeMapWithPath(ScopeName.READ, "a");
        combineScopes(
                source,
                exampleScopeMapWithPath(ScopeName.READ, "b"));

        AddonScope resultingScope = source.get(ScopeName.READ);

        assertThat(resultingScope, allowsAccessTo("/a"));
        assertThat(resultingScope, allowsAccessTo("/b"));
    }

    private Map<ScopeName, AddonScope> exampleScopeMapWithPath(final ScopeName scopeName, final String key)
    {
        Map<ScopeName, AddonScope> map = new HashMap<>();

        PathScopeHelper pathScope = new PathScopeHelper(false, "/" + key);
        AddonScopeApiPath apiPath = new AddonScopeApiPath.ApiPath(Collections.singletonList(pathScope));

        map.put(scopeName, new AddonScope("none", Collections.singletonList(apiPath)));

        return map;
    }

    private Map<ScopeName, AddonScope> mapWithEmptyScopeFromScopeNames(Iterable<ScopeName> scopeNames)
    {
        Map<ScopeName, AddonScope> map = new HashMap<>();
        for (ScopeName scopeName : scopeNames)
        {
            map.put(scopeName, new AddonScope("none", Collections.<AddonScopeApiPath>emptyList()));
        }
        return map;
    }

    private Matcher<? super AddonScope> allowsAccessTo(final String url)
    {
        return new TypeSafeMatcher<AddonScope>()
        {
            @Override
            protected boolean matchesSafely(final AddonScope addonScope)
            {
                HttpServletRequest request = mock(HttpServletRequest.class);

                when(request.getRequestURI()).thenReturn(url);
                when(request.getContextPath()).thenReturn("");
                when(request.getMethod()).thenReturn("GET");
                return addonScope.allow(request);
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("allowed access to " + url);
            }
        };
    }
}
