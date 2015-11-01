package it.matcher;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class IsInteger extends TypeSafeDiagnosingMatcher<String>
{
    private static final IsInteger INSTANCE = new IsInteger();

    public static IsInteger isInteger()
    {
        return INSTANCE;
    }

    private IsInteger()
    {
        super(String.class);
    }

    @Override
    protected boolean matchesSafely(final String string, final Description mismatchDescription)
    {
        if (string == null)
        {
            mismatchDescription.appendText("was null");
            return false;
        }
        if (!StringUtils.isNumeric(string))
        {
            mismatchDescription.appendText("was " + string + " (not a number)");
            return false;
        }
        return true;
    }

    @Override
    public void describeTo(final Description description)
    {
        description.appendText("is an integer");
    }
}
