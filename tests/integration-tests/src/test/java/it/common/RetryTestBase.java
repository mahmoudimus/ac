package it.common;

import org.junit.Rule;
import com.atlassian.testutils.junit.RetryRule;

public class RetryTestBase
{
    @Rule
    public RetryRule retryRule = new RetryRule();

    public static final int MAX_ATTEMPTS = 3;
}
