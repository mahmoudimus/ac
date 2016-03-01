package com.atlassian.plugin.connect.plugin.util.matcher;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;

public class JwtClaimStringMatcher extends ArgumentMatcher<String> {
    private final String claimName;
    private final String expectedValue;
    private final boolean checkValue;

    private JwtClaimStringMatcher(String claimName, String expectedValue, final boolean checkValue) {
        this.claimName = claimName;
        this.expectedValue = expectedValue;
        this.checkValue = checkValue;
    }

    public static JwtClaimStringMatcher hasJwtClaim(String claimName) {
        return new JwtClaimStringMatcher(claimName, null, false);
    }

    public static JwtClaimStringMatcher hasJwtClaimWithValue(String claimName, String expectedValue) {
        return new JwtClaimStringMatcher(claimName, expectedValue, true);
    }

    @Override
    public boolean matches(Object actual) {
        if (!(actual instanceof String)) {
            return false;
        }

        JsonObject json = new JsonParser().parse((String) actual).getAsJsonObject();
        boolean matches = StringUtils.isNotEmpty((String) actual) && json.has(claimName);

        if (matches && checkValue) {
            String actualClaimValue = json.get(claimName).getAsString();
            matches = null == actualClaimValue ? null == expectedValue : actualClaimValue.equals(expectedValue);
        }

        return matches;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("JSON encoded string with claim ").appendValue(claimName);

        if (checkValue) {
            description.appendText(" having value ").appendValue(expectedValue);
        }
    }
}
