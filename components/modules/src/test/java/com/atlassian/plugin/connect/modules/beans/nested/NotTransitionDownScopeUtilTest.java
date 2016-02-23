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
public class NotTransitionDownScopeUtilTest extends BaseScopeUtilTest {
    public NotTransitionDownScopeUtilTest(Object previousTopScope, Object newTopScope, boolean expectedResult) {
        super(previousTopScope, newTopScope, expectedResult);
    }

    @Parameters
    public static Collection<Object[]> parameters() {
        return generateTestParams(READ_OR_LESS, ALL_SCOPES, false);
    }

    @Test
    public void notTransitionDownWhenPreviousReadOrLess() {
        logParams();
        assertThat(isTransitionDownToReadOrLess(previousScopes, newScopes), is(expectedResult));
    }

}
