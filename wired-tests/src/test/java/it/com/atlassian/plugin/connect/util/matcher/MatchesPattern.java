package it.com.atlassian.plugin.connect.util.matcher;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.util.regex.Pattern;

//TODO: Publish a test-jar from this project and consume it in integration tests
//verify https://jira.codehaus.org/browse/MJAR-68 (or whatever is breaking the builds) 
//is not affecting us anymore 

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
