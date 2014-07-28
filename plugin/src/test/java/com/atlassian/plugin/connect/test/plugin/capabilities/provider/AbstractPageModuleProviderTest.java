package com.atlassian.plugin.connect.test.plugin.capabilities.provider;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.ConvertToWiredTest;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.provider.AbstractConnectPageModuleProvider;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectModuleProviderContext;
import com.atlassian.plugin.connect.plugin.capabilities.provider.DefaultConnectModuleProviderContext;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.connect.test.plugin.capabilities.testobjects.PluginForTests;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;

import java.util.List;

import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.test.plugin.capabilities.beans.matchers.WebItemModuleBeanMatchers.hasUrlValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@ConvertToWiredTest
@Ignore("Replace with wired tests")
@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractPageModuleProviderTest<T extends AbstractConnectPageModuleProvider>
{
    private static final String PLUGIN_KEY = "pluginKey";

    @Mock protected WebItemModuleDescriptorFactory webItemModuleDescriptorFactory;
    @Mock protected IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory;
    @Mock protected IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    @Mock protected BundleContext bundleContext;
    @Mock protected ProductAccessor productAccessor;

    private T moduleProvider;

    protected Plugin plugin = new PluginForTests(PLUGIN_KEY, "pluginName");
    protected ConnectAddonBean addon = newConnectAddonBean().withKey(PLUGIN_KEY).build();
    private ConnectModuleProviderContext moduleProviderContext = new DefaultConnectModuleProviderContext(addon);

    private List<ConnectPageModuleBean> beans = ImmutableList.of(
            ConnectPageModuleBean.newPageBean().build()
    );

    @Before
    public void init()
    {
        moduleProvider = createPageModuleProvider();
        when(webItemModuleDescriptorFactory.createModuleDescriptor(moduleProviderContext,
                any(Plugin.class), any(WebItemModuleBean.class))).thenReturn(mock(WebItemModuleDescriptor.class));
    }

    protected abstract T createPageModuleProvider();

    @Test
    public void callsWebItemModuleDescriptorFactoryWithProvidedPlugin()
    {
        verify(webItemModuleDescriptorFactory()).createModuleDescriptor(moduleProviderContext,
                eq(plugin), any(WebItemModuleBean.class));
    }

    @Test
    public void callsWebItemModuleDescriptorFactoryWithProvidedBundleContext()
    {
        verify(webItemModuleDescriptorFactory()).createModuleDescriptor(moduleProviderContext,
                any(Plugin.class), any(WebItemModuleBean.class));
    }

    // establishes that the key of the plugin was used in the construction of the url
    @Test
    public void callsWebItemModuleDescriptorFactoryWithWebItemUrlThatContainsPluginKey()
    {
        verify(webItemModuleDescriptorFactory()).createModuleDescriptor(moduleProviderContext,
                any(Plugin.class), argThat(hasUrlValue("/plugins/servlet/ac/pluginKey/")));
    }

    private WebItemModuleDescriptorFactory webItemModuleDescriptorFactory()
    {
        provideModules();
        return webItemModuleDescriptorFactory;
    }

    private void provideModules()
    {
        moduleProvider.provideModules(moduleProviderContext, plugin, "thePageField", beans);
    }
}
