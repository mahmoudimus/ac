package com.atlassian.plugin.remotable.plugin.module.webfragment;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class TestStringSubstitutor
{
    @Test
    public void testSimpleNoSubstitution()
    {
        assertEquals("hi", new StringSubstitutor().replace("hi", Collections.<String, Object>emptyMap()));
    }

    @Test
    public void testSingleSubstitution()
    {
        assertEquals("hi=joe", new StringSubstitutor().replace("hi=${name}", ImmutableMap.<String, Object>of("name", "joe")));
    }

    @Test
    public void testSingleSubstitutionNumber()
    {
        assertEquals("hi=666.666", new StringSubstitutor().replace("hi=${name}", ImmutableMap.<String, Object>of("name", 666.666f)));
    }

    @Test
    public void testSingleSubstitutionBoolean()
    {
        assertEquals("hi=true", new StringSubstitutor().replace("hi=${name}", ImmutableMap.<String, Object>of("name", true)));
    }

    @Test
    public void testMultiSubstitutionMap()
    {
        assertEquals("hi=joe", new StringSubstitutor().replace("hi=${user.name}",
                ImmutableMap.<String, Object>of("user",
                        ImmutableMap.of("name", "joe")
                )));
    }

    @Test
    public void testEscapingSubstitution()
    {
        assertEquals("?foo=%3F%23&bar=%26%3D#", new StringSubstitutor().replace("?foo=${a}&bar=${b}#",
                ImmutableMap.<String, Object>of("a", "?#", "b", "&=")
        ));
    }

    @Test
    public void testNoMatch()
    {
        assertEquals("hi=", new StringSubstitutor().replace("hi=${foo.bar}", Collections.<String, Object>emptyMap()));
    }

    @Test
    public void testNoMatchDeep()
    {
        assertEquals("hi=", new StringSubstitutor().replace("hi=${foo.bar}",
                ImmutableMap.<String, Object>of("foo", Collections.emptyMap())
        ));
    }
}
