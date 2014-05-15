package com.atlassian.plugin.connect.modules.util;

import java.io.StringWriter;

import com.atlassian.plugin.connect.modules.util.VelocityKiller;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VelocityKillerTest
{
    @Test
    public void testSingleVar() throws Exception
    {
        String raw = "I have a $var";
        String expected = "I have a \\$var";
        
        assertEquals(expected, VelocityKiller.attack(raw));
    }

    @Test
    public void testSingleCurlyVar() throws Exception
    {
        String raw = "I have a ${var}";
        String expected = "I have a \\${var}";

        assertEquals(expected, VelocityKiller.attack(raw));
    }

    @Test
    public void testMultipleMixedVars() throws Exception
    {
        String raw = "I have a $var and some ${other-var}";
        String expected = "I have a \\$var and some \\${other-var}";

        assertEquals(expected, VelocityKiller.attack(raw));
    }

    @Test
    public void testNoVars() throws Exception
    {
        String raw = "I have no vars";
        String expected = "I have no vars";

        assertEquals(expected, VelocityKiller.attack(raw));
    }

    @Test
    public void testOnlyVar() throws Exception
    {
        String raw = "${var}";
        String expected = "\\${var}";

        assertEquals(expected, VelocityKiller.attack(raw));
    }

    @Test
    public void testEmptyString() throws Exception
    {
        String raw = "";
        String expected = "";

        assertEquals(expected, VelocityKiller.attack(raw));
    }

    @Test
    public void testNullString() throws Exception
    {
        String raw = null;
        String expected = null;

        assertEquals(expected, VelocityKiller.attack(raw));
    }

    @Test
    public void testDoubleDollar() throws Exception
    {
        String raw = "I have a $${var}";
        String expected = "I have a $\\${var}";

        assertEquals(expected, VelocityKiller.attack(raw));
    }

    @Test
    public void testWTF() throws Exception
    {
        String raw = "I have a ${$var}";
        String expected = "I have a ${\\$var}";

        assertEquals(expected, VelocityKiller.attack(raw));
    }

    @Test
    public void testNested() throws Exception
    {
        String raw = "I have a ${var inside a ${var} lol}";
        String expected = "I have a ${var inside a \\${var} lol}";

        assertEquals(expected, VelocityKiller.attack(raw));
    }

    @Test
    public void testEmptyVar() throws Exception
    {
        String raw = "I have a ${}";
        String expected = "I have a ${}";

        assertEquals(expected, VelocityKiller.attack(raw));
    }

    @Test
    public void testEscapedVar() throws Exception
    {
        String raw = "I have a \\${var}";
        String expected = "I have a \\\\${var}";

        assertEquals(expected, VelocityKiller.attack(raw));
    }

    @Test
    public void testEscapedEmptyVar() throws Exception
    {
        String raw = "I have a \\${}";
        String expected = "I have a \\${}";

        assertEquals(expected, VelocityKiller.attack(raw));
    }

    @Test
    public void testSebIsCrazy() throws Exception
    {
        String raw = "I have a \\$\\{}";
        String expected = "I have a \\$\\{}";

        assertEquals(expected, VelocityKiller.attack(raw));
    }

    @Test
    public void testAndDillusional() throws Exception
    {
        String raw = "I have a \\$\\{\\}";
        String expected = "I have a \\$\\{\\}";

        assertEquals(expected, VelocityKiller.attack(raw));
    }

    @Test
    public void testDottedVar() throws Exception
    {
        String raw = "I have a $dotted.var and ${another.one}";
        String expected = "I have a \\$dotted.var and \\${another.one}";

        assertEquals(expected, VelocityKiller.attack(raw));
    }

    @Test
    public void testParenVar() throws Exception
    {
        String raw = "I have a $paren() and ${another()}";
        String expected = "I have a \\$paren() and \\${another()}";

        assertEquals(expected, VelocityKiller.attack(raw));
    }

    @Test
    public void testBangVar() throws Exception
    {
        String raw = "I have a $!bang";
        String expected = "I have a \\$!bang";

        assertEquals(expected, VelocityKiller.attack(raw));
    }
}
