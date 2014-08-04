package com.atlassian.plugin.connect.test.plugin.capabilities.provider;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.SpaceToolsTabModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.XWorkActionModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.XWorkInterceptorBean;
import com.atlassian.plugin.connect.plugin.capabilities.ConvertToWiredTest;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.XWorkActionDescriptorFactory;
import com.atlassian.plugin.connect.spi.plugin.capabilities.provider.ConnectModuleProviderContext;
import com.atlassian.plugin.connect.plugin.capabilities.provider.DefaultConnectModuleProviderContext;
import com.atlassian.plugin.connect.plugin.capabilities.provider.SpaceToolsTabModuleProvider;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.plugin.module.confluence.SpaceToolsContextInterceptor;
import com.atlassian.plugin.connect.plugin.module.confluence.SpaceToolsIFrameAction;
import com.atlassian.plugin.connect.plugin.module.page.SpaceToolsTabContext;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;

import java.util.List;

import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.SpaceToolsTabModuleBean.newSpaceToolsTabBean;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@ConvertToWiredTest
@Ignore("convert to wired test")
@RunWith (MockitoJUnitRunner.class)
public class SpaceToolsTabModuleProviderTest
{
    private static final SpaceToolsTabModuleBean DEFAULTS_BEAN = newSpaceToolsTabBean()
            .withName(new I18nProperty("Test Module", null))
            .withUrl("/test.destination")
            .build();

    @Mock private WebItemModuleDescriptorFactory webItemModuleDescriptorFactory;
    @Mock private ProductAccessor productAccessor;
    @Mock private Plugin plugin;
    @Mock private BundleContext bundleContext;
    @Mock private XWorkActionDescriptorFactory xWorkActionDescriptorFactory;
    @Mock private IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    @Mock private IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory;

    private ConnectModuleProviderContext moduleProviderContext;
    private ConnectAddonBean addon;
    private SpaceToolsTabModuleProvider provider;

    @Before
    public void setup()
    {
        this.addon = newConnectAddonBean().withKey("my-plugin").build();
        this.moduleProviderContext = new DefaultConnectModuleProviderContext(addon);
        provider = new SpaceToolsTabModuleProvider(webItemModuleDescriptorFactory, xWorkActionDescriptorFactory,
                productAccessor, iFrameRenderStrategyBuilderFactory, iFrameRenderStrategyRegistry);
        when(plugin.getKey()).thenReturn("my-plugin");
    }

    @Test
    public void testWebItemProperties()
    {
        SpaceToolsTabModuleBean bean = newSpaceToolsTabBean(DEFAULTS_BEAN)
                .withWeight(666)
                .withLocation("test-location")
                .build();

        provider.provideModules(moduleProviderContext, plugin, "spaceTools", ImmutableList.of(bean));

        WebItemBeans webItems = captureWebItemBeans();

        // Space Tools web item
        assertEquals("Test Module", webItems.spaceTools.getName().getValue());
        assertEquals(666, webItems.spaceTools.getWeight());
        assertEquals(SpaceToolsTabModuleProvider.SPACE_TOOLS_SECTION + "/test-location", webItems.spaceTools.getLocation());

        // Space Admin web item
        assertEquals("Test Module", webItems.spaceAdmin.getName().getValue());
        assertTrue(webItems.spaceAdmin.getRawKey().endsWith(SpaceToolsTabModuleProvider.SPACE_ADMIN_KEY_SUFFIX));
        assertEquals(666, webItems.spaceAdmin.getWeight());
        // Space Admin should *always* be in the LEGACY_LOCATION, regardless of any specific location specified by the
        // Space Tab Module bean.
        assertEquals(SpaceToolsTabModuleProvider.SPACE_ADMIN_SECTION + "/" + SpaceToolsTabModuleProvider.LEGACY_LOCATION, webItems.spaceAdmin.getLocation());
    }

    @Test
    public void testWebItemUrl()
    {
        provider.provideModules(moduleProviderContext, plugin, "spaceTools", ImmutableList.of(DEFAULTS_BEAN));

        WebItemBeans webItems = captureWebItemBeans();
        String expectedUrl = "/plugins/atlassian-connect/my-plugin/test-module.action?key=${space.key}";
        assertEquals(expectedUrl, webItems.spaceTools.getUrl());
        assertEquals(expectedUrl, webItems.spaceAdmin.getUrl());
    }

    @Test
    public void testWebItemDefaultWeight()
    {
        when(productAccessor.getPreferredGeneralWeight()).thenReturn(666);
        provider.provideModules(moduleProviderContext, plugin, "spaceTools", ImmutableList.of(DEFAULTS_BEAN));

        WebItemBeans webItems = captureWebItemBeans();
        assertEquals(666, webItems.spaceTools.getWeight());
        assertEquals(666, webItems.spaceAdmin.getWeight());
    }

    @Test
    public void testSpaceToolsWebItemDefaultLocation()
    {
        provider.provideModules(moduleProviderContext, plugin, "spaceTools", ImmutableList.of(DEFAULTS_BEAN));

        WebItemBeans webItems = captureWebItemBeans();
        assertEquals(SpaceToolsTabModuleProvider.SPACE_TOOLS_SECTION + "/" + SpaceToolsTabModuleProvider.DEFAULT_LOCATION, webItems.spaceTools.getLocation());
        assertEquals(SpaceToolsTabModuleProvider.SPACE_ADMIN_SECTION + "/" + SpaceToolsTabModuleProvider.LEGACY_LOCATION, webItems.spaceAdmin.getLocation());
    }

    @Test
    public void testAction()
    {
        provider.provideModules(moduleProviderContext, plugin, "spaceTools", ImmutableList.of(DEFAULTS_BEAN));
        XWorkActionModuleBean actionModuleBean = captureActionBean();

        assertEquals("/plugins/atlassian-connect/my-plugin", actionModuleBean.getNamespace());
        assertEquals("test-module", actionModuleBean.getRawKey());
        assertEquals(SpaceToolsIFrameAction.class, actionModuleBean.getClazz());
    }

    @Test
    @Ignore
    public void testActionContextData()
    {
        provider.provideModules(moduleProviderContext, plugin, "spaceTools", ImmutableList.of(DEFAULTS_BEAN));
        XWorkActionModuleBean actionModuleBean = captureActionBean();

        SpaceToolsTabContext context = (SpaceToolsTabContext) actionModuleBean.getParameters().get("context");
//        assertSame(plugin, context.getPlugin());
//        assertEquals(DEFAULTS_BEAN.getUrl(), context.getUrl());
        assertTrue(context.getSpaceAdminWebItemKey().endsWith(SpaceToolsTabModuleProvider.SPACE_ADMIN_KEY_SUFFIX));

//        PageInfo pageInfo = context.getPageInfo();
//        assertEquals(DEFAULTS_BEAN.getName().getValue(), pageInfo.getTitle());
    }

    @Test
    public void testActionInterceptor()
    {
        provider.provideModules(moduleProviderContext, plugin, "spaceTools", ImmutableList.of(DEFAULTS_BEAN));
        XWorkActionModuleBean actionModuleBean = captureActionBean();

        XWorkInterceptorBean interceptorBean = actionModuleBean.getInterceptorsBeans().get(0);
        assertEquals("space-context", interceptorBean.getName());
        assertEquals(SpaceToolsContextInterceptor.class, interceptorBean.getClazz());
    }

    private XWorkActionModuleBean captureActionBean()
    {
        ArgumentCaptor<XWorkActionModuleBean> captor = ArgumentCaptor.forClass(XWorkActionModuleBean.class);
        verify(xWorkActionDescriptorFactory).create(addon, eq(plugin), captor.capture());
        return captor.getValue();
    }

    private WebItemBeans captureWebItemBeans()
    {
        ArgumentCaptor<WebItemModuleBean> captor = ArgumentCaptor.forClass(WebItemModuleBean.class);
        verify(webItemModuleDescriptorFactory, times(2)).createModuleDescriptor(moduleProviderContext, eq(plugin), captor.capture());
        List<WebItemModuleBean> beans = captor.getAllValues();
        return new WebItemBeans(beans.get(0), beans.get(1));
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
