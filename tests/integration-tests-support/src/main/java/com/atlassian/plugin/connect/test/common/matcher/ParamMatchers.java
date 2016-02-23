package com.atlassian.plugin.connect.test.common.matcher;

import org.hamcrest.Matcher;

import java.util.TimeZone;

import static com.atlassian.plugin.connect.test.common.matcher.MatchesPattern.matchesPattern;
import static org.hamcrest.Matchers.isIn;

/**
 * Matchers for parameters passed to Connect add-ons.
 */
public class ParamMatchers {

    public static Matcher<String> isTimeZone() {
        return isIn(TimeZone.getAvailableIDs());
    }

    public static Matcher<String> isLocale() {
        return matchesPattern("[A-Za-z0-9]{2,}-[A-Za-z0-9]{2,}");
    }

    public static Matcher<String> isVersionNumber() {
        return matchesPattern("(\\d)*\\.(\\d)*\\.(\\d)*.*");
    }
}
