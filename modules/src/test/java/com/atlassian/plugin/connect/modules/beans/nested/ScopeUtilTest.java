package com.atlassian.plugin.connect.modules.beans.nested;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static com.atlassian.plugin.connect.modules.beans.nested.ScopeUtil.isTransitionDownToReadOrLess;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ScopeUtilTest
{
    private static final Object NULL_SCOPE = new Object();
    private static final Set<ScopeName> EMPTY = ImmutableSet.of();
    private static final Set<Object> ALL_SCOPES = Sets.union(ImmutableSet.of(NULL_SCOPE),
            ImmutableSet.copyOf(Arrays.asList(ScopeName.values())));
    private static final Set<Object> READ_OR_LESS = Sets.newHashSet(NULL_SCOPE, ScopeName.READ);
    private static final Set<Object> GREATER_THAN_READ = ImmutableSet.copyOf(Iterables.filter(ALL_SCOPES, new Predicate<Object>()
    {
        @Override
        public boolean apply(@Nullable Object scope)
        {
            return !READ_OR_LESS.contains(scope);
        }
    }));

    // Not transition down when previous is read or less
    private static Collection<Object[]> PARAMS_NOT_TRANSITION_DOWN_WHEN_PREVIOUS_READ_OR_LESS =
            generateTestParams(READ_OR_LESS, ALL_SCOPES, false);

    private final boolean expectedResult;
    private final Set<ScopeName> previousScopes;
    private final Set<ScopeName> newScopes;

    public ScopeUtilTest(Object previousTopScope, Object newTopScope, boolean expectedResult)
    {
        this.expectedResult = expectedResult;
        this.previousScopes = set(previousTopScope);
        this.newScopes = set(newTopScope);
    }

    private static Set<ScopeName> set(Object scope)
    {
        return scope == NULL_SCOPE ? EMPTY : ImmutableSet.of((ScopeName)scope);
    }

    private static Collection<Object[]> generateTestParams(Set<Object> prev, Set<Object> nuevo, final boolean expected)
    {
        final Set<List<Object>> variants = Sets.cartesianProduct(prev, nuevo);
        final Iterable<Object[]> params = Iterables.transform(variants, new Function<List<Object>, Object[]>()
        {
            @Override
            public Object[] apply(@Nullable List<Object> variant)
            {
                return new Object[]{variant.get(0), variant.get(1), expected};
            }
        });
        return ImmutableList.copyOf(params);
    }

    @Parameters
    public static Collection<Object[]> parameters()
    {
        return PARAMS_NOT_TRANSITION_DOWN_WHEN_PREVIOUS_READ_OR_LESS;
    }

    @Test
    public void notTransitionDownWhenPreviousReadOrLess()
    {
        System.out.println("notTransitionDownWhenPreviousReadOrLess: " + previousScopes + "; " + newScopes + "; " + expectedResult);
        assertThat(isTransitionDownToReadOrLess(previousScopes, newScopes), is(expectedResult));
    }

}
