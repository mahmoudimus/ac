package it.modules;

import com.atlassian.plugin.connect.test.pageobjects.AbstractConnectIFrameComponent;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import org.hamcrest.collection.IsMapContaining;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static it.matcher.ParamMatchers.isLocale;
import static it.matcher.ParamMatchers.isTimeZone;
import static it.matcher.ParamMatchers.isVersionNumber;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class ConnectAsserts
{

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

    public static void verifyIframeURLHasVersionNumber(AbstractConnectIFrameComponent<?> connectIframeComponent)
    {
        String version = connectIframeComponent.getFromQueryString("cv");
        assertThat(version, isVersionNumber());
    }

    public static void verifyStandardAddOnRelativeQueryParameters(final RemoteWebItem webItem, String contextPath)
    {
        Map<String, String> parameters = Maps.newHashMap();
        for (String key : new String[] {"tz", "loc", "cp", "lic", "cv"})
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
        assertThat(parameters, IsMapContaining.hasEntry(is("cv"), isVersionNumber()));
    }

}
