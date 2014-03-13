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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseScopeUtilTest
{
    private static final Logger log = LoggerFactory.getLogger(BaseScopeUtilTest.class);

    private static final Object NULL_SCOPE = new Object();
    private static final Set<ScopeName> EMPTY = ImmutableSet.of();

    protected static final Set<Object> ALL_SCOPES = Sets.union(ImmutableSet.of(NULL_SCOPE),
            ImmutableSet.copyOf(Arrays.asList(ScopeName.values())));

    protected static final Set<Object> READ_OR_LESS = Sets.newHashSet(NULL_SCOPE, ScopeName.READ);

    protected static final Set<Object> GREATER_THAN_READ = ImmutableSet.copyOf(Iterables.filter(ALL_SCOPES, new Predicate<Object>()
    {
        @Override
        public boolean apply(@Nullable Object scope)
        {
            return !READ_OR_LESS.contains(scope);
        }
    }));

    protected final boolean expectedResult;
    protected final Set<ScopeName> previousScopes;
    protected final Set<ScopeName> newScopes;

    public BaseScopeUtilTest(Object previousTopScope, Object newTopScope, boolean expectedResult)
    {
        this.previousScopes = set(previousTopScope);
        this.newScopes = set(newTopScope);
        this.expectedResult = expectedResult;
    }

    protected static Set<ScopeName> set(Object scope)
    {
        return scope == NULL_SCOPE ? EMPTY : ImmutableSet.of((ScopeName) scope);
    }

    protected static Collection<Object[]> generateTestParams(Set<Object> prev, Set<Object> nuevo, final boolean expected)
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

    protected void logParams()
    {
        log.trace("params: " + previousScopes + " -> " + newScopes + " : " + expectedResult);
    }
}
