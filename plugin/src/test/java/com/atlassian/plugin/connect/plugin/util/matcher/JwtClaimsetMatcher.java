package com.atlassian.plugin.connect.plugin.util.matcher;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class JwtClaimsetMatcher extends ArgumentMatcher<String>
{
    private final Set<String> expectedClaimNames;

    private JwtClaimsetMatcher(String... expectedClaimNames)
    {
        this.expectedClaimNames = new HashSet<String>(Arrays.asList(expectedClaimNames));
    }

    public static JwtClaimsetMatcher hasJwtClaimset(String... expectedClaimNames)
    {
        return new JwtClaimsetMatcher(expectedClaimNames);
    }

    @Override
    public boolean matches(Object argument)
    {
        assertThat(argument, is(instanceOf(String.class)));
        try
        {
            JSONObject jsonObject = (JSONObject) new JSONParser(JSONParser.MODE_JSON_SIMPLE).parse((String) argument);
            return jsonObject.keySet().equals(expectedClaimNames);
        }
        catch (ParseException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void describeTo(Description description)
    {
        description.appendText("Expecting exactly these claims: ");
        description.appendValueList("[", ",", "]", expectedClaimNames);
    }
}
