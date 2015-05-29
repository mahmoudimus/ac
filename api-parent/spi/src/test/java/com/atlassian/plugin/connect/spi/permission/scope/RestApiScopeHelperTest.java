package com.atlassian.plugin.connect.spi.permission.scope;

import com.atlassian.plugin.connect.spi.scope.helper.RestApiScopeHelper;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RestApiScopeHelperTest
{
    @Test
    public void nullIsNotVersionString() throws Exception
    {
        assertFalse(RestApiScopeHelper.isVersionString(null));
    }

    @Test
    public void emptyIsNotVersionString() throws Exception
    {
        assertFalse(RestApiScopeHelper.isVersionString(""));
    }

    @Test
    public void stringIsNotVersionString() throws Exception
    {
        assertFalse(RestApiScopeHelper.isVersionString("api"));
    }

    @Test
    public void latestIsVersionString() throws Exception
    {
        assertTrue(RestApiScopeHelper.isVersionString("latest"));
    }

    @Test
    public void latestUppercaseIsVersionString() throws Exception
    {
        assertTrue(RestApiScopeHelper.isVersionString("LATEST"));
    }

    @Test
    public void singleDigitIsVersionString() throws Exception
    {
        assertTrue(RestApiScopeHelper.isVersionString("1"));
    }

    @Test
    public void doubleDigitIsVersionString() throws Exception
    {
        assertTrue(RestApiScopeHelper.isVersionString("1.0"));
    }

    @Test
    public void singleDigitAlphaSuffixIsVersionString() throws Exception
    {
        assertTrue(RestApiScopeHelper.isVersionString("2.beta3"));
    }

    @Test
    public void doubleDigitAlphaSuffixIsVersionString() throws Exception
    {
        assertTrue(RestApiScopeHelper.isVersionString("2.0.alpha1"));
    }

    @Test
    public void singleDigitHyphenatedSuffixIsVersionString() throws Exception
    {
        assertTrue(RestApiScopeHelper.isVersionString("2-m1"));
    }

    @Test
    public void doubleDigitHyphenatedSuffixIsVersionString() throws Exception
    {
        assertTrue(RestApiScopeHelper.isVersionString("2.0-m1"));
    }

    @Test
    public void singleDigitDoubleHyphenatedSuffixIsVersionString() throws Exception
    {
        assertTrue(RestApiScopeHelper.isVersionString("2-m1-SNAPSHOT"));
    }

    @Test
    public void doubleDigitDoubleHyphenatedSuffixIsVersionString() throws Exception
    {
        assertTrue(RestApiScopeHelper.isVersionString("2.0-m1-SNAPSHOT"));
    }

    @Test
    public void tripleDigitDoubleHyphenatedSuffixIsVersionString() throws Exception
    {
        assertTrue(RestApiScopeHelper.isVersionString("2.1.0-m1-SNAPSHOT"));
    }
}
