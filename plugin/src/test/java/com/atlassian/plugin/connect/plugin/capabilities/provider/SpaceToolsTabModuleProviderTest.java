package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.XWorkActionDescriptorFactory;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;

import java.util.List;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class SpaceToolsTabModuleProviderTest
{
    private static final ConnectPageModuleBean DEFAULTS_BEAN = newPageBean()
            .withName(new I18nProperty("Test Module", null))
            .withUrl("/test.destination")
            .build();

    @Mock private WebItemModuleDescriptorFactory webItemModuleDescriptorFactory;
    @Mock private ProductAccessor productAccessor;
    @Mock private Plugin plugin;
    @Mock private BundleContext bundleContext;
    @Mock private XWorkActionDescriptorFactory xWorkActionDescriptorFactory;

    private SpaceToolsTabModuleProvider provider;

    @Before
    public void setup()
    {
        provider = new SpaceToolsTabModuleProvider(webItemModuleDescriptorFactory, xWorkActionDescriptorFactory, productAccessor);
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

        WebItemBeans webItems = captureWebItemBeans();

        // Space Tools web item
        assertEquals("Test Module", webItems.spaceTools.getName().getValue());
        assertEquals("test-module", webItems.spaceTools.getKey());
        assertEquals(666, webItems.spaceTools.getWeight());
        assertEquals(SpaceToolsTabModuleProvider.SPACE_TOOLS_SECTION + "/test-location", webItems.spaceTools.getLocation());

        // Space Admin web item
        assertEquals("Test Module", webItems.spaceAdmin.getName().getValue());
        assertEquals("test-module" + SpaceToolsTabModuleProvider.SPACE_ADMIN_KEY_SUFFIX, webItems.spaceAdmin.getKey());
        assertEquals(666, webItems.spaceAdmin.getWeight());
        // Space Admin should *always* be in the LEGACY_LOCATION, regardless of any specific location specified by the
        // Space Tab Module bean.
        assertEquals(SpaceToolsTabModuleProvider.SPACE_ADMIN_SECTION + "/" + SpaceToolsTabModuleProvider.LEGACY_LOCATION, webItems.spaceAdmin.getLocation());
    }

    @Test
    public void testWebItemUrl()
    {
        provider.provideModules(plugin, bundleContext, "spaceTools", ImmutableList.of(DEFAULTS_BEAN));

        WebItemBeans webItems = captureWebItemBeans();
        String expectedUrl = "/plugins/atlassian-connect/my-plugin/test-module.action?key=${space.key}";
        assertEquals(expectedUrl, webItems.spaceTools.getUrl());
        assertEquals(expectedUrl, webItems.spaceAdmin.getUrl());
    }

    @Test
    public void testWebItemDefaultWeight()
    {
        when(productAccessor.getPreferredGeneralWeight()).thenReturn(666);
        provider.provideModules(plugin, bundleContext, "spaceTools", ImmutableList.of(DEFAULTS_BEAN));

        WebItemBeans webItems = captureWebItemBeans();
        assertEquals(666, webItems.spaceTools.getWeight());
        assertEquals(666, webItems.spaceAdmin.getWeight());
    }

    @Test
    public void testSpaceToolsWebItemDefaultLocation()
    {
        provider.provideModules(plugin, bundleContext, "spaceTools", ImmutableList.of(DEFAULTS_BEAN));

        WebItemBeans webItems = captureWebItemBeans();
        assertEquals(SpaceToolsTabModuleProvider.SPACE_TOOLS_SECTION + "/" + SpaceToolsTabModuleProvider.DEFAULT_LOCATION, webItems.spaceTools.getLocation());
        assertEquals(SpaceToolsTabModuleProvider.SPACE_ADMIN_SECTION + "/" + SpaceToolsTabModuleProvider.LEGACY_LOCATION, webItems.spaceAdmin.getLocation());
    }
//
//    @Test
//    public void testActionProperties()
//    {
//        provider.provideModules(plugin, bundleContext, "spaceTools", ImmutableList.of(DEFAULTS_BEAN));
//
//        verify(spaceToolsActionDescriptorFactory).create(plugin, "test-module", "test-module" + SpaceToolsTabModuleProvider.SPACE_ADMIN_KEY_SUFFIX, "Test Module", "/test.destination");
//    }

    private WebItemBeans captureWebItemBeans()
    {
        ArgumentCaptor<WebItemModuleBean> captor = ArgumentCaptor.forClass(WebItemModuleBean.class);
        verify(webItemModuleDescriptorFactory, times(2)).createModuleDescriptor(eq(plugin), eq(bundleContext), captor.capture());
        List<WebItemModuleBean> beans = captor.getAllValues();
        return new WebItemBeans(beans.remove(0), beans.remove(0));
    }

    private class WebItemBeans
    {
        WebItemModuleBean spaceTools;
        WebItemModuleBean spaceAdmin;

        private WebItemBeans(WebItemModuleBean spaceTools, WebItemModuleBean spaceAdmin)
        {
            this.spaceTools = spaceTools;
            this.spaceAdmin = spaceAdmin;
        }
    }
}
