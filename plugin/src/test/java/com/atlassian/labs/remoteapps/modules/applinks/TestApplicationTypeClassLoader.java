package com.atlassian.labs.remoteapps.modules.applinks;

import com.atlassian.applinks.spi.application.TypeId;
import com.atlassian.applinks.spi.manifest.ManifestNotFoundException;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestApplicationTypeClassLoader
{
    private ApplicationTypeClassLoader cl;

    @Before
    public void setUp()
    {
        cl = new ApplicationTypeClassLoader();
    }
    @Test
    public void testUniqueClassesForKeys() throws IllegalAccessException, InstantiationException
    {
        assertTrue(cl.getApplicationType("foo") == cl.getApplicationType("foo"));
        assertTrue(cl.getApplicationType("foo.bar") == cl.getApplicationType("foo.bar"));
        assertFalse(cl.getApplicationType("foo") == cl.getApplicationType("bar"));
    }

    @Test
    public void testManifestGeneration() throws IllegalAccessException, InstantiationException,
            ManifestNotFoundException
    {
        RemoteAppApplicationType appType = new RemoteAppApplicationType(new TypeId("foo"), "foo",
                URI.create("/foo.png"), null);

        assertTrue("foo".equals(cl.getManifestProducer(appType).newInstance().getManifest(
                URI.create("/foo")).getTypeId().get()));

        assertTrue(cl.getManifestProducer(appType) == cl.getManifestProducer(appType));
    }

    @Test
    public void testManifestGenerationWithDotInKeys() throws IllegalAccessException, InstantiationException,
            ManifestNotFoundException
    {
        RemoteAppApplicationType appType = new RemoteAppApplicationType(new TypeId("foo.bar"), "foo.bar",
                URI.create("/foo.bar.png"), null);

        assertTrue("foo.bar".equals(cl.getManifestProducer(appType).newInstance().getManifest(
                URI.create("/foo.bar")).getTypeId().get()));
    }
}
