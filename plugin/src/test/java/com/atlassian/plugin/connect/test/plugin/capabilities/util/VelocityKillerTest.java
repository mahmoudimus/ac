package com.atlassian.plugin.connect.test.plugin.capabilities.util;

import com.atlassian.plugin.connect.plugin.capabilities.util.VelocityKiller;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VelocityKillerTest
{
    @Test
    public void testSingleVar() throws Exception
    {
        String raw = "I have a $var";
        String expected = "I have a var";
        
        assertEquals(expected, VelocityKiller.attack(raw));
    }

    @Test
    public void testSingleCurlyVar() throws Exception
    {
        String raw = "I have a ${var}";
        String expected = "I have a {var}";

        assertEquals(expected, VelocityKiller.attack(raw));
    }

    @Test
    public void testMultipleMixedVars() throws Exception
    {
        String raw = "I have a $var and some ${other-var}";
        String expected = "I have a var and some {other-var}";

        assertEquals(expected, VelocityKiller.attack(raw));
    }

    @Test
    public void testNoVars() throws Exception
    {
        String raw = "I have no vars";
        String expected = "I have no vars";

        assertEquals(expected, VelocityKiller.attack(raw));
    }
}
