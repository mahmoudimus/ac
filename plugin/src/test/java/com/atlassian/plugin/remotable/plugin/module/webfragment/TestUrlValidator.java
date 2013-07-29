package com.atlassian.plugin.remotable.plugin.module.webfragment;

import org.junit.Test;

public class TestUrlValidator
{
    @Test
    public void testValid()
    {
        new UrlValidator(new UrlVariableSubstitutor()).validate("/blah/${ermagherd}");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid()
    {
        new UrlValidator(new UrlVariableSubstitutor()).validate("/blah/{ermagherd}");
    }
}
