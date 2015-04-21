package com.atlassian.plugin.connect.test.plugin.module.confluence;

import com.atlassian.confluence.event.events.plugin.XWorkStateChangeEvent;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.confluence.capabilities.provider.XWorkPackageCreator;
import com.atlassian.plugin.connect.confluence.capabilities.XWorkActionDescriptor;
import com.atlassian.plugin.connect.confluence.iframe.SpaceToolsTabContext;
import com.opensymphony.xwork.config.Configuration;
import com.opensymphony.xwork.config.ConfigurationManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith (MockitoJUnitRunner.class)
public class XWorkActionDescriptorTest
{
    @Mock private EventPublisher eventPublisher;
    @Mock private Plugin plugin;
    @Mock private SpaceToolsTabContext context;
    @Mock private XWorkPackageCreator xWorkPackageCreator;

    private XWorkActionDescriptor descriptor;

    @Before
    public void setup()
    {
        ConfigurationManager.clearConfigurationProviders();
        assertEquals(1, ConfigurationManager.getConfigurationProviders().size());
        descriptor = new XWorkActionDescriptor(eventPublisher, plugin, "action-test-module", xWorkPackageCreator);
    }

    @Test
    public void testEnable()
    {
        descriptor.enabled();
        // 2 because ConfigurationManager lazily inserts the default config provider when you call getConfigurationProviders
        assertEquals(2, ConfigurationManager.getConfigurationProviders().size());
        assertTrue(ConfigurationManager.getConfigurationProviders().contains(descriptor));

        verify(eventPublisher).publish(any(XWorkStateChangeEvent.class));
    }

    @Test
    public void testDisable()
    {
        ConfigurationManager.addConfigurationProvider(descriptor);
        descriptor.disabled();
        // 1 because ConfigurationManager lazily inserts the default config provider when you call getConfigurationProviders
        assertEquals(1, ConfigurationManager.getConfigurationProviders().size());

        verify(eventPublisher).publish(any(XWorkStateChangeEvent.class));
    }

    @Test
    public void testInit()
    {
        Configuration configuration = mock(Configuration.class);
        descriptor.init(configuration);

        verify(xWorkPackageCreator).createAndRegister(eq(configuration));
    }
}
