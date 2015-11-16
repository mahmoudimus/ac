package com.atlassian.plugin.connect.modules.beans.nested;

import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static com.atlassian.plugin.connect.modules.beans.nested.ScopeUtil.isTransitionDownToReadOrLess;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class IsTransitionDownScopeUtilTest extends BaseScopeUtilTest
{
    public IsTransitionDownScopeUtilTest(Object previousTopScope, Object newTopScope, boolean expectedResult)
    {
        super(previousTopScope, newTopScope, expectedResult);
    }

    @Parameters
    public static Collection<Object[]> parameters()
    {
        return generateTestParams(GREATER_THAN_READ, READ_OR_LESS, true);
    }

    @Test
    public void isTransitionDownWhenPreviousGreaterThanReadAndNewReadOrLess()
    {
        logParams();
        assertThat(isTransitionDownToReadOrLess(previousScopes, newScopes), is(expectedResult));
    }

}
