package com.atlassian.plugin.connect.plugin.util.matcher;

import com.google.common.base.Objects;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class JwtContextClaimMatcher extends ArgumentMatcher<String>
{
    private final String key;
    private final String expectedValue;

    public JwtContextClaimMatcher(String key, String expectedValue)
    {
        this.key = key;
        this.expectedValue = expectedValue;
    }

    public JwtContextClaimMatcher(String key)
    {
        this.key = key;
        this.expectedValue = null;
    }

    public static ArgumentMatcher<String> hasJwtContextKeyWithValue(String key, String value)
    {
        return new JwtContextClaimMatcher(key, value);
    }

    public static ArgumentMatcher<String> hasJwtContextKey(String key)
    {
        return new JwtContextClaimMatcher(key);
    }

    @Override
    public boolean matches(Object argument)
    {
        assertThat(argument, is(instanceOf(String.class)));
        try
        {
            JSONObject jsonObject = (JSONObject) new JSONParser(JSONParser.MODE_JSON_SIMPLE).parse((String) argument);
            boolean hasClaim = jsonObject.containsKey("context");
            if (hasClaim)
            {
                JSONObject contextClaim = (JSONObject) jsonObject.get("context");
                // Nested searching of json keys. eg, "foo.bar" does: object.get('foo').get('bar')
                // Doesn't currently handle keys which have literal . characters in the value, eg object.get('foo.bar') (meh)
                String[] keys = key.split("\\.");
                Object value = contextClaim;
                for (String key : keys)
                {
                    if (value instanceof JSONObject)
                    {
                        value = ((JSONObject) value).get(key);
                    } else if (value == null) {
                        return false;
                    } else {
                        throw new RuntimeException(value.toString() + " is not a JSONObject and could not be deserialised");
                    }
                }
                return expectedValue == null ? value != null : Objects.equal(expectedValue, value);
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
        description.appendText("Expected context of " + key + " to be " + expectedValue);
    }
}
