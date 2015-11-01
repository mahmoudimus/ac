package it.matcher;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class IsLong extends TypeSafeDiagnosingMatcher<String>
{
    private static final IsLong INSTANCE = new IsLong();

    public static IsLong isLong()
    {
        return INSTANCE;
    }

    private IsLong()
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

        try
        {
            Long.valueOf((String)string);
        }
        catch (NumberFormatException e)
        {
            mismatchDescription.appendText("was " + string + " (not a long)");
            return false;
        }

        return true;
    }

    @Override
    public void describeTo(final Description description)
    {
        description.appendText("is a long");
    }
}
