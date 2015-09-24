package com.atlassian.plugin.connect.plugin.util.matcher;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;

public class JwtClaimMatcher extends ArgumentMatcher<String>
{
    private final String claimName;
    private final String expectedValue;
    private final boolean checkValue;

    private JwtClaimMatcher(String claimName, String expectedValue, final boolean checkValue)
    {
        this.claimName = claimName;
        this.expectedValue = expectedValue;
        this.checkValue = checkValue;
    }

    public static JwtClaimMatcher hasJwtClaim(String claimName)
    {
        return new JwtClaimMatcher(claimName, null, false);
    }

    public static JwtClaimMatcher hasJwtClaimWithValue(String claimName, String expectedValue)
    {
        return new JwtClaimMatcher(claimName, expectedValue, true);
    }

    @Override
    public boolean matches(Object actual)
    {
        JsonObject json = new JsonParser().parse((String) actual).getAsJsonObject();
        boolean matches = actual instanceof String
                && !StringUtils.isEmpty((String) actual)
                && json.has(claimName);

        if (matches && checkValue)
        {
            String actualClaimValue = json.get(claimName).getAsString();
            matches = null == actualClaimValue ? null == expectedValue : actualClaimValue.equals(expectedValue);
        }

        return matches;
    }

    @Override
    public void describeTo(Description description)
    {
        description.appendText("JSON encoded string with claim ").appendValue(claimName);

        if (checkValue)
        {
            description.appendText(" having value ").appendValue(expectedValue);
        }
    }
}
