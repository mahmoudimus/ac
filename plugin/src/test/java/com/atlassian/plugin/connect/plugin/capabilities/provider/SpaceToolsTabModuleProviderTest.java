package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.SpaceToolsActionDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectPageModuleBean.newPageBean;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SpaceToolsTabModuleProviderTest
{
    private static final ConnectPageModuleBean DEFAULTS_BEAN = newPageBean()
        .withName(new I18nProperty("Test Module", null))
        .withUrl("/test.destination")
        .build();

    @Mock private WebItemModuleDescriptorFactory webItemModuleDescriptorFactory;
    @Mock private SpaceToolsActionDescriptorFactory spaceToolsActionDescriptorFactory;
    @Mock private ProductAccessor productAccessor;
    @Mock private Plugin plugin;
    @Mock private BundleContext bundleContext;

    private SpaceToolsTabModuleProvider provider;

    @Before
    public void setup()
    {
        provider = new SpaceToolsTabModuleProvider(webItemModuleDescriptorFactory, spaceToolsActionDescriptorFactory, productAccessor);
        when(plugin.getKey()).thenReturn("my-plugin");
    }

    @Test
    public void testWebItemProperties()
    {
        ConnectPageModuleBean bean = newPageBean(DEFAULTS_BEAN)
            .withWeight(666)
            .withLocation("test-location")
            .build();

        provider.provideModules(plugin, bundleContext, "spaceTools", ImmutableList.of(bean));

        WebItemModuleBean webItemBean = captureWebItemBean();
        assertEquals("Test Module", webItemBean.getName().getValue());
        assertEquals("test-module", webItemBean.getKey());
        assertEquals(666, webItemBean.getWeight());
        assertEquals(SpaceToolsTabModuleProvider.SPACE_TOOLS_SECTION + "/test-location", webItemBean.getLocation());
    }

    @Test
    public void testWebItemUrl()
    {
        provider.provideModules(plugin, bundleContext, "spaceTools", ImmutableList.of(DEFAULTS_BEAN));

        WebItemModuleBean webItemBean = captureWebItemBean();
        assertEquals(SpaceToolsActionDescriptorFactory.NAMESPACE_PREFIX + "my-plugin/test-module.action?key=${space.key}", webItemBean.getUrl());
    }

    @Test
    public void testWebItemDefaultWeight()
    {
        when(productAccessor.getPreferredGeneralWeight()).thenReturn(666);
        provider.provideModules(plugin, bundleContext, "spaceTools", ImmutableList.of(DEFAULTS_BEAN));

        WebItemModuleBean webItemBean = captureWebItemBean();
        assertEquals(666, webItemBean.getWeight());
    }

    @Test
    public void testWebItemDefaultLocation()
    {
        provider.provideModules(plugin, bundleContext, "spaceTools", ImmutableList.of(DEFAULTS_BEAN));

        WebItemModuleBean webItemBean = captureWebItemBean();
        assertEquals(SpaceToolsTabModuleProvider.SPACE_TOOLS_SECTION + "/" + SpaceToolsTabModuleProvider.DEFAULT_LOCATION, webItemBean.getLocation());
    }

    @Test
    public void testActionProperties()
    {
        provider.provideModules(plugin, bundleContext, "spaceTools", ImmutableList.of(DEFAULTS_BEAN));

        verify(spaceToolsActionDescriptorFactory).create(plugin, "test-module", "Test Module", "/test.destination");
    }

    private WebItemModuleBean captureWebItemBean()
    {
        ArgumentCaptor<WebItemModuleBean> captor = ArgumentCaptor.forClass(WebItemModuleBean.class);
        verify(webItemModuleDescriptorFactory).createModuleDescriptor(eq(plugin), eq(bundleContext), captor.capture());
        return captor.getValue();
    }
}
