package com.atlassian.plugin.connect.modules.beans.nested;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static com.atlassian.plugin.connect.modules.beans.nested.ScopeUtil.isTransitionUpFromReadOrLess;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class IsTransitionUpScopeUtilTest extends BaseScopeUtilTest {

    public IsTransitionUpScopeUtilTest(Object previousTopScope, Object newTopScope, boolean expectedResult) {
        super(previousTopScope, newTopScope, expectedResult);
    }

    @Parameters
    public static Collection<Object[]> parameters() {
        return generateTestParams(READ_OR_LESS, GREATER_THAN_READ, true);
    }

    @Test
    public void notTransitionUpWhenPreviousReadOrLessAndNewGreaterThanRead() {
        logParams();
        assertThat(isTransitionUpFromReadOrLess(previousScopes, newScopes), is(expectedResult));
    }
}
