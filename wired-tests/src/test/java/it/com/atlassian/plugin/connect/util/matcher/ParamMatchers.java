package it.com.atlassian.plugin.connect.util.matcher;

import org.hamcrest.Matcher;

//TODO: Publish a test-jar from this project and consume it in integration tests
//verify https://jira.codehaus.org/browse/MJAR-68 (or whatever is breaking the builds) 
//is not affecting us anymore 

/**
 * Matchers for parameters passed to Connect add-ons.
 */
public class ParamMatchers
{
    public static Matcher<String> isVersionNumber()
    {
        return MatchesPattern.matchesPattern("(\\d)*\\.(\\d)*\\.(\\d)*.*");
    }
}
