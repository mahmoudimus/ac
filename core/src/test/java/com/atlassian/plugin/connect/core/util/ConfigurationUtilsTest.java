package com.atlassian.plugin.connect.core.util;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ConfigurationUtilsTest
{
    private final String NAME = ConfigurationUtilsTest.class.getSimpleName();

    @Before
    public void beforeEachTest()
    {
        System.clearProperty(NAME);
    }

    @Test
    public void canParsePositiveIntegerSystemProperty()
    {
        System.setProperty(NAME, "1234");
        assertThat(ConfigurationUtils.getIntSystemProperty(NAME, -1), is(1234));
    }

    @Test
    public void canParseNegativeIntegerSystemProperty()
    {
        System.setProperty(NAME, "-4567");
        assertThat(ConfigurationUtils.getIntSystemProperty(NAME, -1), is(-4567));
    }

    @Test
    public void canParseZeroIntegerSystemProperty()
    {
        System.setProperty(NAME, "0");
        assertThat(ConfigurationUtils.getIntSystemProperty(NAME, -1), is(0));
    }

    @Test
    public void canParseIntegerSystemPropertyWithWhiteSpace()
    {
        System.setProperty(NAME, " 1234 ");
        assertThat(ConfigurationUtils.getIntSystemProperty(NAME, -1), is(1234));
    }

    @Test
    public void usesDefaultIfSystemPropertyIsMissing()
    {
        assertThat(ConfigurationUtils.getIntSystemProperty(NAME, -1), is(-1));
    }

    @Test
    public void usesDefaultIfSystemPropertyIsBlank()
    {
        System.setProperty(NAME, "");
        assertThat(ConfigurationUtils.getIntSystemProperty(NAME, -2), is(-2));
    }

    @Test
    public void usesDefaultIfSystemPropertyIsWhiteSpace()
    {
        System.setProperty(NAME, " ");
        assertThat(ConfigurationUtils.getIntSystemProperty(NAME, -3), is(-3));
    }

    @Test
    public void usesDefaultIfSystemPropertyIsGarbled()
    {
        System.setProperty(NAME, "wrong");
        assertThat(ConfigurationUtils.getIntSystemProperty(NAME, -4), is(-4));
    }
}
