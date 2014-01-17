package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.IFrameServletBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.IFramePageServletDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.testobjects.PluginForTests;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.servlet.descriptors.ServletModuleDescriptor;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;

import java.util.List;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.matchers.WebItemModuleBeanMatchers.hasUrlValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractPageModuleProviderTest<T extends AbstractConnectPageModuleProvider>
{
    private static final String PLUGIN_KEY = "pluginKey";

    @Mock protected WebItemModuleDescriptorFactory webItemModuleDescriptorFactory;
    @Mock protected IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory;
    @Mock protected IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    @Mock protected IFramePageServletDescriptorFactory servletDescriptorFactory;
    @Mock protected BundleContext bundleContext;
    @Mock protected ProductAccessor productAccessor;

    private T moduleProvider;

    protected Plugin plugin = new PluginForTests(PLUGIN_KEY, "pluginName");

    private List<ConnectPageModuleBean> beans = ImmutableList.of(
            ConnectPageModuleBean.newPageBean().build()
    );

    @Before
    public void init()
    {
        moduleProvider = createPageModuleProvider();
        when(webItemModuleDescriptorFactory.createModuleDescriptor(any(Plugin.class), any(BundleContext.class),
                any(WebItemModuleBean.class))).thenReturn(mock(WebItemModuleDescriptor.class));
        when(servletDescriptorFactory.createIFrameServletDescriptor(any(Plugin.class), any(IFrameServletBean.class)))
                .thenReturn(mock(ServletModuleDescriptor.class));
    }

    protected abstract T createPageModuleProvider();

    @Test
    public void callsWebItemModuleDescriptorFactoryWithProvidedPlugin()
    {
        verify(webItemModuleDescriptorFactory()).createModuleDescriptor(eq(plugin), any(BundleContext.class),
                any(WebItemModuleBean.class));
    }

    @Test
    public void callsWebItemModuleDescriptorFactoryWithProvidedBundleContext()
    {
        verify(webItemModuleDescriptorFactory()).createModuleDescriptor(any(Plugin.class), eq(bundleContext),
                any(WebItemModuleBean.class));
    }

    // establishes that the key of the plugin was used in the construction of the url
    @Test
    public void callsWebItemModuleDescriptorFactoryWithWebItemUrlThatContainsPluginKey()
    {
        verify(webItemModuleDescriptorFactory()).createModuleDescriptor(any(Plugin.class), any(BundleContext.class),
                argThat(hasUrlValue("/plugins/servlet/ac/pluginKey/")));
    }

    @Test
    public void callsServletDescriptorFactoryWithProvidedPlugin()
    {
        verify(servletDescriptorFactory()).createIFrameServletDescriptor(eq(plugin), any(IFrameServletBean.class));
    }

    protected IFramePageServletDescriptorFactory servletDescriptorFactory()
    {
        provideModules();
        return servletDescriptorFactory;
    }

    private WebItemModuleDescriptorFactory webItemModuleDescriptorFactory()
    {
        provideModules();
        return webItemModuleDescriptorFactory;
    }

    private void provideModules()
    {
        moduleProvider.provideModules(plugin, bundleContext, "thePageField", beans);
    }
}
