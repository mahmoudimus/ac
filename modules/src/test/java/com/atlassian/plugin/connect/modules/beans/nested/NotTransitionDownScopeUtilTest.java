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
public class NotTransitionDownScopeUtilTest extends BaseScopeUtilTest
{
    public NotTransitionDownScopeUtilTest(Object previousTopScope, Object newTopScope, boolean expectedResult)
    {
        super(previousTopScope, newTopScope, expectedResult);
    }

    @Parameters
    public static Collection<Object[]> parameters()
    {
        return generateTestParams(READ_OR_LESS, ALL_SCOPES, false);
    }

    @Test
    public void notTransitionDownWhenPreviousReadOrLess()
    {
        logParams();
        assertThat(isTransitionDownToReadOrLess(previousScopes, newScopes), is(expectedResult));
    }

}
