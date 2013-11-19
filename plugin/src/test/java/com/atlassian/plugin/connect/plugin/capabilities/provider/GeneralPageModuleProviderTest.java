package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectPageCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.IFrameServletBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.IFramePageServletDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.testobjects.PluginForTests;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.servlet.descriptors.ServletModuleDescriptor;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;

import java.util.List;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.matchers.WebItemCapabilityBeanMatchers.hasUrlValue;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class GeneralPageModuleProviderTest
{
    private static final String PLUGIN_KEY = "pluginKey";
    @Mock
    private WebItemModuleDescriptorFactory webItemModuleDescriptorFactory;

    @Mock
    private IFramePageServletDescriptorFactory servletDescriptorFactory;

    @Mock
    private BundleContext bundleContext;

    @Mock
    private ProductAccessor productAccessor;

    private GeneralPageModuleProvider moduleProvider;
    private Plugin plugin = new PluginForTests(PLUGIN_KEY, "pluginName");

    private List<ConnectPageCapabilityBean> beans = ImmutableList.of(
            ConnectPageCapabilityBean.newPageBean().build()
    );

    @Before
    public void init()
    {
        moduleProvider = new GeneralPageModuleProvider(webItemModuleDescriptorFactory, servletDescriptorFactory, productAccessor);
        when(webItemModuleDescriptorFactory.createModuleDescriptor(any(Plugin.class), any(BundleContext.class),
                any(WebItemCapabilityBean.class))).thenReturn(mock(WebItemModuleDescriptor.class));
        when(servletDescriptorFactory.createIFrameServletDescriptor(any(Plugin.class), any(IFrameServletBean.class)))
                .thenReturn(mock(ServletModuleDescriptor.class));
    }

    @Test
    public void callsWebItemModuleDescriptorFactoryWithProvidedPlugin()
    {
        verify(webItemModuleDescriptorFactory()).createModuleDescriptor(eq(plugin), any(BundleContext.class),
                any(WebItemCapabilityBean.class));
    }

    @Test
    public void callsWebItemModuleDescriptorFactoryWithProvidedBundleContext()
    {
        verify(webItemModuleDescriptorFactory()).createModuleDescriptor(any(Plugin.class), eq(bundleContext),
                any(WebItemCapabilityBean.class));
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

    @Test
    public void callsServletDescriptorFactoryWithGeneralPageParamSet()
    {
        verify(servletDescriptorFactory()).createIFrameServletDescriptor(eq(plugin), argThat(hasGeneralParamSet()));
    }

    // handling explicitly as only one test on it
    private ArgumentMatcher<IFrameServletBean> hasGeneralParamSet()
    {
        return new ArgumentMatcher<IFrameServletBean>()
        {
            @Override
            public boolean matches(Object item)
            {
                assertThat(item, is(instanceOf(IFrameServletBean.class)));
                IFrameServletBean iFrameServletBean = (IFrameServletBean) item;
                return Objects.equal(iFrameServletBean.getiFrameParams().getAsMap().get("general"), "1");
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("IFrameServletBean with iframe param param general = 1");
            }
        };
    }

    private IFramePageServletDescriptorFactory servletDescriptorFactory()
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
