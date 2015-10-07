package com.atlassian.plugin.connect.confluence.provider;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.spi.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.capabilities.provider.AbstractConnectPageModuleProvider;
import com.atlassian.plugin.connect.spi.condition.PageConditionsFactory;
import com.atlassian.plugin.connect.spi.module.ConnectModuleProviderContext;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.connect.util.annotation.ConvertToWiredTest;
import com.atlassian.plugin.connect.util.fixture.PluginForTests;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static com.atlassian.plugin.connect.confluence.capabilities.bean.matchers.WebItemModuleBeanMatchers.hasUrlValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ConvertToWiredTest
@Ignore("Replace with wired tests")
@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractPageModuleProviderTest<T extends AbstractConnectPageModuleProvider>
{
    private static final String PLUGIN_KEY = "pluginKey";

    @Mock protected WebItemModuleDescriptorFactory webItemModuleDescriptorFactory;
    @Mock protected IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory;
    @Mock protected IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    @Mock protected ProductAccessor productAccessor;
    @Mock protected PageConditionsFactory pageConditionsFactory;

    private T moduleProvider;

    protected Plugin plugin = new PluginForTests(PLUGIN_KEY, "pluginName");
    private ConnectModuleProviderContext moduleProviderContext = mock(ConnectModuleProviderContext.class);

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
        moduleProvider.createPluginModuleDescriptors(beans, plugin, moduleProviderContext);
    }
}
