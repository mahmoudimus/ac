package it.matcher;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class IsNotBlank extends TypeSafeDiagnosingMatcher<String>
{
    private static final IsNotBlank INSTANCE = new IsNotBlank();

    public static IsNotBlank isNotBlank()
    {
        return INSTANCE;
    }

    private IsNotBlank()
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
        if (string.isEmpty())
        {
            mismatchDescription.appendText("was empty");
            return false;
        }
        if (StringUtils.isBlank(string))
        {
            mismatchDescription.appendText("was blank");
            return false;
        }
        return true;
    }

    @Override
    public void describeTo(final Description description)
    {
        description.appendText("is not null or a blank string");
    }
}
