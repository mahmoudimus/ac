package com.atlassian.plugin.connect.test.common.matcher;

import java.util.regex.Pattern;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class MatchesPattern extends TypeSafeDiagnosingMatcher<String>
{
    public static MatchesPattern matchesPattern(String pattern)
    {
        return matchesPattern(Pattern.compile(pattern));
    }

    public static MatchesPattern matchesPattern(Pattern pattern)
    {
        return new MatchesPattern(pattern);
    }

    private final Pattern pattern;

    private MatchesPattern(Pattern pattern)
    {
        super(String.class);
        this.pattern = pattern;
    }

    @Override
    protected boolean matchesSafely(final String string, final Description mismatchDescription)
    {
        if (!pattern.matcher(string).matches())
        {
            mismatchDescription.appendText("was " + string);
            return false;
        }
        return true;
    }

    @Override
    public void describeTo(final Description description)
    {
        description.appendText("matches pattern " + pattern.pattern());
    }
}
