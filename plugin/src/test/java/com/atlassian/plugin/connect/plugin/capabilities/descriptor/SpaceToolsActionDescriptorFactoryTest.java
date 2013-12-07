package com.atlassian.plugin.connect.plugin.capabilities.descriptor;


import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.module.confluence.SpaceToolsActionDescriptor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class SpaceToolsActionDescriptorFactoryTest
{
    @Mock private Plugin plugin;

    private SpaceToolsActionDescriptorFactory factory;

    @Before
    public void setup()
    {
        factory = new SpaceToolsActionDescriptorFactory(null);
    }

    @Test
    public void testCreate()
    {
        SpaceToolsActionDescriptor descriptor = factory.create(plugin, "test-module", "Test Module", "/test-module");

        assertEquals("action-test-module", descriptor.getKey());
    }
}
