package it.com.atlassian.plugin.connect.util;

import org.hamcrest.Matcher;

import static it.com.atlassian.plugin.connect.util.MatchesPattern.matchesPattern;

//TODO: Publish a test-jar from this project and consume it in integration tests
//verify https://jira.codehaus.org/browse/MJAR-68 (or whatever is breaking the builds) 
//is not affecting us anymore 
/**
 * Matchers for parameters passed to Connect add-ons.
 */
public class ParamMatchers
{
    public static Matcher<String> isTimeZone()
    {
        return matchesPattern("[A-Za-z0-9_\\-]+/[A-Za-z0-9_\\-]+");
    }

    public static Matcher<String> isLocale()
    {
        return matchesPattern("[A-Za-z0-9]{2,}-[A-Za-z0-9]{2,}");
    }

    public static Matcher<String> isVersionNumber()
    {
        // jgitflow creates versions like x.y.z-SNAPSHOT-RC, so we'll just check
        // the beginning of the string looks like a version number
        return matchesPattern("(\\d)*\\.(\\d)*\\.(\\d)*");
    }
}
