package com.atlassian.labs.remoteapps.util;

import org.junit.Test;

import static com.atlassian.labs.remoteapps.util.IdUtils.dashesToCamelCase;
import static com.atlassian.labs.remoteapps.util.IdUtils.dashesToTitle;
import static org.junit.Assert.assertEquals;

public class TestIdUtils
{
    @Test
    public void testSingleWord()
    {
        assertEquals("Bob", dashesToCamelCase("bob"));
        assertEquals("Bob", dashesToTitle("bob"));
    }

    @Test
    public void testTwoWords()
    {
        assertEquals("FooBar", dashesToCamelCase("foo-bar"));
        assertEquals("Foo Bar", dashesToTitle("foo-bar"));
    }
}
