package com.atlassian.plugin.connect.plugin.module.confluence;

import com.atlassian.confluence.event.events.plugin.XWorkStateChangeEvent;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.module.page.SpaceToolsTabContext;

import com.opensymphony.webwork.dispatcher.VelocityResult;
import com.opensymphony.xwork.config.Configuration;
import com.opensymphony.xwork.config.ConfigurationManager;
import com.opensymphony.xwork.config.entities.ActionConfig;
import com.opensymphony.xwork.config.entities.PackageConfig;
import com.opensymphony.xwork.config.entities.ResultConfig;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith (MockitoJUnitRunner.class)
public class SpaceToolsActionDescriptorTest
{
    @Mock private EventPublisher eventPublisher;
    @Mock private Plugin plugin;
    @Mock private SpaceToolsTabContext context;

    private SpaceToolsActionDescriptor descriptor;

    @Before
    public void setup()
    {
        ConfigurationManager.clearConfigurationProviders();
        assertEquals(1, ConfigurationManager.getConfigurationProviders().size());
        descriptor = new SpaceToolsActionDescriptor(eventPublisher, plugin, "action-test-module", context, "/ac/test", "test-module");
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
    public void testXWorkConfiguration()
    {
        Configuration configuration = mock(Configuration.class);
        descriptor.init(configuration);

        ArgumentCaptor<PackageConfig> captor = ArgumentCaptor.forClass(PackageConfig.class);
        verify(configuration).addPackageConfig(eq("action-test-module"), captor.capture());
        PackageConfig packageConfig = captor.getValue();

        assertEquals("/ac/test", packageConfig.getNamespace());
        assertEquals("action-test-module", packageConfig.getName());

        assertEquals(1, packageConfig.getActionConfigs().size());
        ActionConfig actionConfig = (ActionConfig)packageConfig.getActionConfigs().values().iterator().next();
        assertEquals(SpaceToolsIFrameAction.class.getName(), actionConfig.getClassName());
        assertTrue(actionConfig.getParams().containsKey("context"));
        assertSame(context, actionConfig.getParams().get("context"));

        assertEquals(1, actionConfig.getResults().size());
        ResultConfig resultConfig = (ResultConfig)actionConfig.getResults().get("success");
        assertEquals(VelocityResult.class.getName(), resultConfig.getClassName());

        assertThat(actionConfig.getInterceptors(), hasItem((Matcher)instanceOf(SpaceToolsContextInterceptor.class)));
    }
}
