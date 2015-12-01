package com.atlassian.plugin.connect.plugin.util.matcher;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class JwtClaimLongMatcher extends ArgumentMatcher<String>
{
    private final String claimName;
    private final long expectedClaimValue;
    private final long tolerance;

    private JwtClaimLongMatcher(String claimName, long expectedClaimValue, long tolerance)
    {
        this.claimName = claimName;
        this.expectedClaimValue = expectedClaimValue;
        this.tolerance = tolerance;
    }

    public static JwtClaimLongMatcher hasJwtClaimWithLongValue(String claimName, long expectedValue, long tolerance)
    {
        return new JwtClaimLongMatcher(claimName, expectedValue, tolerance);
    }

    @Override
    public boolean matches(Object argument)
    {
        assertThat(argument, is(instanceOf(String.class)));
        try
        {
            JSONObject jsonObject = (JSONObject) new JSONParser(JSONParser.MODE_JSON_SIMPLE).parse((String) argument);

            if (jsonObject.containsKey(claimName))
            {
                Object actualClaimValue = jsonObject.get(claimName);
                Long actualClaimLong = null;

                if (actualClaimValue instanceof Number)
                {
                    actualClaimLong = ((Number) actualClaimValue).longValue();
                }
                else if (null != actualClaimValue)
                {
                    actualClaimLong = Long.parseLong(actualClaimValue.toString());
                }

                if (null != actualClaimLong)
                {
                    return Math.abs(actualClaimLong - expectedClaimValue) <= tolerance;
                }
            }

            return false;
        }
        catch (ParseException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void describeTo(Description description)
    {
        description.appendText(String.format("Expecting claim \"%s\" to be within %d of %d", claimName, tolerance, expectedClaimValue));
    }
}
