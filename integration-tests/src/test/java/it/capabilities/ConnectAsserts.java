package it.capabilities;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.hamcrest.collection.IsMapContaining;

import static it.matcher.ParamMatchers.isLocale;
import static it.matcher.ParamMatchers.isTimeZone;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ConnectAsserts
{
    public static void assertURIEquals(String expectedString, String actualString)
    {
        URI expectedURI = URI.create(expectedString);
        URI actualURI = URI.create(actualString);

        assertEquals("URI schemes do not match", expectedURI.getScheme(), actualURI.getScheme());
        assertEquals("URI hosts do not match", expectedURI.getHost(), actualURI.getHost());
        assertEquals("URI fragments do not match", expectedURI.getFragment(), actualURI.getFragment());

        Map<String,String> expectedParams = getQueryStringAsMap(expectedURI.getQuery());
        Map<String,String> actualParms = getQueryStringAsMap(actualURI.getQuery());

        assertEquals("URI query param lengths do not match",expectedParams.size(),actualParms.size());
        for(Map.Entry<String,String> entry : actualParms.entrySet())
        {
            assertTrue("actual URI query param '" + entry.getKey() + "'missing from expected params",expectedParams.containsKey(entry.getKey()));
            assertEquals("actual URI query param value does not match expected value",expectedParams.get(entry.getKey()),entry.getValue());
        }
    }

    private static Map<String, String> getQueryStringAsMap(String query)
    {
        Map<String, String> map = new HashMap<String, String>();
        if (Strings.isNullOrEmpty(query))
        {
            return map;
        }

        String[] queryParams = query.split("&");
        for (String param : queryParams)
        {
            String[] nvp = param.split("=");
            if (nvp.length < 2)
            {
                map.put(nvp[0], "");
            }
            else
            {
                map.put(nvp[0], nvp[1]);
            }
        }
        return map;
    }

    public static void verifyStandardAddOnRelativeQueryParameters(final RemoteWebItem webItem, String contextPath)
    {
        Map<String, String> parameters = Maps.newHashMap();
        for (String key : new String[] {"tz", "loc", "cp", "lic"})
        {
            parameters.put(key, webItem.getFromQueryString(key));
        }
        verifyContainsStandardAddOnQueryParamters(parameters, contextPath);
    }

    public static void verifyContainsStandardAddOnQueryParamters(Map<String, String> parameters, String contextPath)
    {
        //example tz:  America/Los_Angeles
        //example loc: en-GB
        assertThat(parameters, IsMapContaining.hasEntry(is("tz"), isTimeZone()));
        assertThat(parameters, IsMapContaining.hasEntry(is("loc"), isLocale()));
        assertThat(parameters, IsMapContaining.hasEntry(is("cp"), is(contextPath)));
        assertThat(parameters, IsMapContaining.hasEntry(is("lic"), is("none")));
    }

}
