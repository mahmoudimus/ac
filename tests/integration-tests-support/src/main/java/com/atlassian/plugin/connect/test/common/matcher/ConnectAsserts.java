package com.atlassian.plugin.connect.test.common.matcher;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.plugin.connect.test.common.pageobjects.RemoteWebItem;
import com.atlassian.plugin.connect.test.common.pageobjects.AbstractConnectIFrameComponent;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import org.hamcrest.collection.IsMapContaining;

import static com.atlassian.plugin.connect.test.common.matcher.ParamMatchers.isLocale;
import static com.atlassian.plugin.connect.test.common.matcher.ParamMatchers.isTimeZone;
import static com.atlassian.plugin.connect.test.common.matcher.ParamMatchers.isVersionNumber;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ConnectAsserts {

    private static Map<String, String> getQueryStringAsMap(String query) {
        Map<String, String> map = new HashMap<String, String>();
        if (Strings.isNullOrEmpty(query)) {
            return map;
        }

        String[] queryParams = query.split("&");
        for (String param : queryParams) {
            String[] nvp = param.split("=");
            if (nvp.length < 2) {
                map.put(nvp[0], "");
            } else {
                map.put(nvp[0], nvp[1]);
            }
        }
        return map;
    }

    public static void verifyIframeURLHasVersionNumber(AbstractConnectIFrameComponent<?> connectIframeComponent) {
        String version = connectIframeComponent.getFromQueryString("cv");
        assertThat(version, isVersionNumber());
    }

    public static void verifyStandardAddonRelativeQueryParameters(final RemoteWebItem webItem, String contextPath) {
        Map<String, String> parameters = Maps.newHashMap();
        for (String key : new String[]{"tz", "loc", "cp", "lic", "cv"}) {
            parameters.put(key, webItem.getFromQueryString(key));
        }
        verifyContainsStandardAddonQueryParameters(parameters, contextPath);
    }

    public static void verifyContainsStandardAddonQueryParameters(Map<String, String> parameters, String contextPath) {
        //example tz:  America/Los_Angeles
        //example loc: en-GB
        assertThat(parameters, IsMapContaining.hasEntry(is("tz"), isTimeZone()));
        assertThat(parameters, IsMapContaining.hasEntry(is("loc"), isLocale()));
        assertThat(parameters, IsMapContaining.hasEntry(is("cp"), is(contextPath)));
        assertThat(parameters, IsMapContaining.hasEntry(is("lic"), is("none")));
        assertThat(parameters, IsMapContaining.hasEntry(is("cv"), isVersionNumber()));
    }

}
