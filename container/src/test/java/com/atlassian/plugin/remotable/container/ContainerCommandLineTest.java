package com.atlassian.plugin.remotable.container;

import com.google.common.collect.Iterables;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public final class ContainerCommandLineTest
{
    @Test
    public void testParsePortWithLongOption() throws Exception
    {
        testParsePort(new String[]{"--port", "1234", "app1", "app2"});
    }

    @Test
    public void testParsePortWithShortOption() throws Exception
    {
        testParsePort(new String[]{"--p", "1234", "app1", "app2"});
    }

    @Test(expected = IllegalStateException.class)
    public void testParsePortWhichIsNotANumber() throws Exception
    {
        testParsePort(new String[]{"--p", "PORT", "app1", "app2"});
    }

    private void testParsePort(String[] args)
    {
        final ContainerCommandLine parsed = ContainerCommandLine.parse(args);
        assertEquals(1234, parsed.getPort().get().intValue());
        assertEquals(2, Iterables.size(parsed.getApplications()));
        assertTrue(Iterables.contains(parsed.getApplications(), "app1"));
        assertTrue(Iterables.contains(parsed.getApplications(), "app2"));
    }
}
