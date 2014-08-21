package com.atlassian.plugin.connect.test.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.ConvertToWiredTest;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConditionModuleFragmentFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.IconModuleFragmentFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ParamsModuleFragmentFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectModuleProviderContext;
import com.atlassian.plugin.connect.plugin.capabilities.provider.DefaultConnectModuleProviderContext;
import com.atlassian.plugin.connect.plugin.capabilities.provider.DefaultWebItemModuleProvider;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.spi.module.DynamicMarkerCondition;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.connect.test.plugin.capabilities.ConnectAsserts;
import com.atlassian.plugin.connect.test.plugin.capabilities.testobjects.PluginForTests;
import com.atlassian.plugin.connect.test.plugin.capabilities.testobjects.RemotablePluginAccessorFactoryForTests;
import com.atlassian.plugin.connect.test.plugin.capabilities.testobjects.descriptor.WebItemModuleDescriptorFactoryForTests;
import com.atlassian.plugin.web.WebFragmentHelper;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.sal.api.ApplicationProperties;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;

import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since 1.0
 */
@ConvertToWiredTest
@Ignore("convert to wired test")
public class WebItemProviderTest
{
    private static final String JSON_FIELD_NAME = "webItems";

    private Plugin plugin;
    private WebInterfaceManager webInterfaceManager;
    private WebFragmentHelper webFragmentHelper;
    private WebItemModuleDescriptorFactory webItemFactory;
    private HttpServletRequest servletRequest;
    private IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory;
    private IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    private ConnectModuleProviderContext moduleProviderContext;
    private ConnectAddonBean addon;
    @Mock private ApplicationProperties applicationProperties;

    @Before
    public void setup() throws Exception
    {
        plugin = new PluginForTests("my-key", "My Plugin");
        this.addon = newConnectAddonBean().withKey("my-key").build();
        this.moduleProviderContext = new DefaultConnectModuleProviderContext(addon);
        RemotablePluginAccessorFactoryForTests remotablePluginAccessorFactoryForTests = new RemotablePluginAccessorFactoryForTests();
        webInterfaceManager = mock(WebInterfaceManager.class);
        webFragmentHelper = mock(WebFragmentHelper.class);
        iFrameRenderStrategyBuilderFactory = mock(IFrameRenderStrategyBuilderFactory.class);
        iFrameRenderStrategyRegistry = mock(IFrameRenderStrategyRegistry.class);

        webItemFactory = new WebItemModuleDescriptorFactory(
                new WebItemModuleDescriptorFactoryForTests(webInterfaceManager),
                new IconModuleFragmentFactory(remotablePluginAccessorFactoryForTests),
                new ConditionModuleFragmentFactory(mock(ProductAccessor.class), new ParamsModuleFragmentFactory()),
                new ParamsModuleFragmentFactory());
        servletRequest = mock(HttpServletRequest.class);

        when(webInterfaceManager.getWebFragmentHelper()).thenReturn(webFragmentHelper);

        when(webFragmentHelper.renderVelocityFragment(anyString(), anyMap())).thenAnswer(
                new Answer<Object>()
                {
                    @Override
                    public Object answer(InvocationOnMock invocationOnMock) throws Throwable
                    {
                        Object[] args = invocationOnMock.getArguments();
                        return (String) args[0];
                    }
                }
        );

        when(webFragmentHelper.loadCondition(anyString(), any(Plugin.class))).thenReturn(new DynamicMarkerCondition());

        when(servletRequest.getContextPath()).thenReturn("http://ondemand.com/jira");
    }

    @Test
    public void singleAbsoluteLink() throws Exception
    {
        WebItemModuleBean bean = newWebItemBean()
                .withName(new I18nProperty("My Web Item", "my.webitem"))
                .withUrl("http://www.google.com")
                .withLocation("atl.admin/menu")
                .build();

        DefaultWebItemModuleProvider moduleProvider = new DefaultWebItemModuleProvider(webItemFactory, iFrameRenderStrategyBuilderFactory, iFrameRenderStrategyRegistry, applicationProperties);

        List<ModuleDescriptor> descriptors = moduleProvider.provideModules(moduleProviderContext, plugin, JSON_FIELD_NAME, newArrayList(bean));

        assertEquals(1, descriptors.size());

        WebItemModuleDescriptor descriptor = (WebItemModuleDescriptor) descriptors.get(0);
        descriptor.enabled();

        assertEquals("http://www.google.com", descriptor.getLink().getDisplayableUrl(servletRequest, new HashMap<String, Object>()));
    }

    @Test
    public void singleAbsoluteLinkWithAddOnContext() throws Exception
    {
        WebItemModuleBean bean = newWebItemBean()
                .withName(new I18nProperty("My Web Item", "my.webitem"))
                .withUrl("http://www.google.com")
                .withLocation("atl.admin/menu")
                .withContext(AddOnUrlContext.page)
                .build();

        DefaultWebItemModuleProvider moduleProvider = new DefaultWebItemModuleProvider(webItemFactory, iFrameRenderStrategyBuilderFactory, iFrameRenderStrategyRegistry, applicationProperties);

        List<ModuleDescriptor> descriptors = moduleProvider.provideModules(moduleProviderContext, plugin, JSON_FIELD_NAME, newArrayList(bean));

        assertEquals(1, descriptors.size());

        WebItemModuleDescriptor descriptor = (WebItemModuleDescriptor) descriptors.get(0);
        descriptor.enabled();

        assertEquals("http://www.google.com", descriptor.getLink().getDisplayableUrl(servletRequest, new HashMap<String, Object>()));
    }

    @Test
    public void singleAbsoluteLinkWithProductContext() throws Exception
    {
        WebItemModuleBean bean = newWebItemBean()
                .withName(new I18nProperty("My Web Item", "my.webitem"))
                .withUrl("http://www.google.com")
                .withLocation("atl.admin/menu")
                .withContext(AddOnUrlContext.product)
                .build();

        DefaultWebItemModuleProvider moduleProvider = new DefaultWebItemModuleProvider(webItemFactory, iFrameRenderStrategyBuilderFactory, iFrameRenderStrategyRegistry, applicationProperties);

        List<ModuleDescriptor> descriptors = moduleProvider.provideModules(moduleProviderContext, plugin, JSON_FIELD_NAME, newArrayList(bean));

        assertEquals(1, descriptors.size());

        WebItemModuleDescriptor descriptor = (WebItemModuleDescriptor) descriptors.get(0);
        descriptor.enabled();

        assertEquals("http://www.google.com", descriptor.getLink().getDisplayableUrl(servletRequest, new HashMap<String, Object>()));
    }

    @Test
    public void singleAddOnLink() throws Exception
    {
        WebItemModuleBean bean = newWebItemBean()
                .withName(new I18nProperty("My Web Item", "my.webitem"))
                .withUrl("/some/admin")
                .withLocation("atl.admin/menu")
                .build();

        DefaultWebItemModuleProvider moduleProvider = new DefaultWebItemModuleProvider(webItemFactory, iFrameRenderStrategyBuilderFactory, iFrameRenderStrategyRegistry, applicationProperties);

        List<ModuleDescriptor> descriptors = moduleProvider.provideModules(moduleProviderContext, plugin, JSON_FIELD_NAME, newArrayList(bean));

        assertEquals(1, descriptors.size());

        WebItemModuleDescriptor descriptor = (WebItemModuleDescriptor) descriptors.get(0);
        descriptor.enabled();

        assertEquals("http://ondemand.com/jira/plugins/servlet/ac/my-key/some/admin", descriptor.getLink().getDisplayableUrl(servletRequest, new HashMap<String, Object>()));
    }

    @Test
    public void singleAddOnLinkWithAddOnContext() throws Exception
    {
        WebItemModuleBean bean = newWebItemBean()
                .withName(new I18nProperty("My Web Item", "my.webitem"))
                .withUrl("/some/admin")
                .withLocation("atl.admin/menu")
                .withContext(AddOnUrlContext.page)
                .build();

        DefaultWebItemModuleProvider moduleProvider = new DefaultWebItemModuleProvider(webItemFactory, iFrameRenderStrategyBuilderFactory, iFrameRenderStrategyRegistry, applicationProperties);

        List<ModuleDescriptor> descriptors = moduleProvider.provideModules(moduleProviderContext, plugin, JSON_FIELD_NAME, newArrayList(bean));

        assertEquals(1, descriptors.size());

        WebItemModuleDescriptor descriptor = (WebItemModuleDescriptor) descriptors.get(0);
        descriptor.enabled();

        ConnectAsserts.assertURIEquals("http://ondemand.com/jira/plugins/servlet/ac/my-key/some/admin", descriptor.getLink().getDisplayableUrl(servletRequest, new HashMap<String, Object>()));
    }

    @Test
    public void singleProductLink() throws Exception
    {
        WebItemModuleBean bean = newWebItemBean()
                .withName(new I18nProperty("My Web Item", "my.webitem"))
                .withUrl("/local/jira/admin")
                .withLocation("atl.admin/menu")
                .withContext(AddOnUrlContext.product)
                .build();

        DefaultWebItemModuleProvider moduleProvider = new DefaultWebItemModuleProvider(webItemFactory, iFrameRenderStrategyBuilderFactory, iFrameRenderStrategyRegistry, applicationProperties);

        List<ModuleDescriptor> descriptors = moduleProvider.provideModules(moduleProviderContext, plugin, JSON_FIELD_NAME, newArrayList(bean));

        assertEquals(1, descriptors.size());

        WebItemModuleDescriptor descriptor = (WebItemModuleDescriptor) descriptors.get(0);
        descriptor.enabled();

        assertEquals("http://ondemand.com/jira/local/jira/admin", descriptor.getLink().getDisplayableUrl(servletRequest, new HashMap<String, Object>()));
    }

    @Test
    public void multipleWebItems() throws Exception
    {
        WebItemModuleBean bean = newWebItemBean()
                .withName(new I18nProperty("My Web Item", "my.webitem"))
                .withUrl("http://www.google.com")
                .withLocation("atl.admin/menu")
                .build();

        WebItemModuleBean bean2 = newWebItemBean()
                .withName(new I18nProperty("My Other Web Item", "my.other.webitem"))
                .withUrl("/my/addon")
                .withLocation("atl.admin/menu")
                .build();

        DefaultWebItemModuleProvider moduleProvider = new DefaultWebItemModuleProvider(webItemFactory, iFrameRenderStrategyBuilderFactory, iFrameRenderStrategyRegistry, applicationProperties);

        List<ModuleDescriptor> descriptors = moduleProvider.provideModules(moduleProviderContext, plugin, JSON_FIELD_NAME, newArrayList(bean, bean2));

        assertEquals(2, descriptors.size());

        WebItemModuleDescriptor descriptor = (WebItemModuleDescriptor) descriptors.get(0);
        descriptor.enabled();

        WebItemModuleDescriptor descriptor2 = (WebItemModuleDescriptor) descriptors.get(1);
        descriptor2.enabled();

        assertEquals("http://www.google.com", descriptor.getLink().getDisplayableUrl(servletRequest, new HashMap<String, Object>()));
        assertEquals("http://ondemand.com/jira/plugins/servlet/ac/my-key/my/addon", descriptor2.getLink().getDisplayableUrl(servletRequest, new HashMap<String, Object>()));
    }

}
