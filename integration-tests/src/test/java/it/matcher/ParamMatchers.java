package it.matcher;

import org.hamcrest.Matcher;

import static it.matcher.MatchesPattern.matchesPattern;

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
        return matchesPattern("(\\d)*\\.(\\d)*\\.(\\d)*.*");
    }
}
