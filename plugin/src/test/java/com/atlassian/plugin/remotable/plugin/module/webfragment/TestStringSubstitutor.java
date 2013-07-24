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
        assertEquals("hi=joe", new StringSubstitutor().replace("hi=[name]", ImmutableMap.<String, Object>of("name", "joe")));
    }

    @Test
    public void testSingleSubstitutionNumber()
    {
        assertEquals("hi=666.666", new StringSubstitutor().replace("hi=[name]", ImmutableMap.<String, Object>of("name", 666.666f)));
    }

    @Test
    public void testSingleSubstitutionBoolean()
    {
        assertEquals("hi=true", new StringSubstitutor().replace("hi=[name]", ImmutableMap.<String, Object>of("name", true)));
    }

    @Test
    public void testMultiSubstitutionMap()
    {
        assertEquals("hi=joe", new StringSubstitutor().replace("hi=[user.name]",
                ImmutableMap.<String, Object>of("user",
                        ImmutableMap.of("name", "joe")
                )));
    }

    @Test
    public void testMultiSubstitutionBean()
    {
        assertEquals("hi=joe", new StringSubstitutor().replace("hi=[value.value]",
                new MyBean(
                        new MyBean("joe")
                )));
    }

    private static class MyBean<T>
    {
        private final T value;

        private MyBean(T value)
        {
            this.value = value;
        }

        public T getValue()
        {
            return value;
        }
    }
}
