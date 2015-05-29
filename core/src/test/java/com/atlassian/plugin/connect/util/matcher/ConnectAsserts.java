package com.atlassian.plugin.connect.util.matcher;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Strings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @since 1.0
 */
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
}
