package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConditionModuleFragmentFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.IconModuleFragmentFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ParamsModuleFragmentFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.RelativeAddOnUrlConverter;
import com.atlassian.plugin.connect.plugin.capabilities.testobjects.PluginForTests;
import com.atlassian.plugin.connect.plugin.capabilities.testobjects.RemotablePluginAccessorFactoryForTests;
import com.atlassian.plugin.connect.plugin.capabilities.testobjects.descriptor.WebItemModuleDescriptorFactoryForTests;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.spi.module.DynamicMarkerCondition;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.web.WebFragmentHelper;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.BundleContext;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;

import static com.atlassian.plugin.connect.plugin.capabilities.ConnectAsserts.assertURIEquals;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemCapabilityBean.newWebItemBean;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since 1.0
 */
public class WebItemProviderTest
{
    private static final String JSON_FIELD_NAME = "webItems";
    
    Plugin plugin;
    WebInterfaceManager webInterfaceManager;
    WebFragmentHelper webFragmentHelper;
    WebItemModuleDescriptorFactory webItemFactory;
    HttpServletRequest servletRequest;
    
    @Before
    public void setup() throws Exception
    {
        plugin = new PluginForTests("my-key", "My Plugin");
        RemotablePluginAccessorFactoryForTests remotablePluginAccessorFactoryForTests = new RemotablePluginAccessorFactoryForTests();
        webInterfaceManager = mock(WebInterfaceManager.class);
        webFragmentHelper = mock(WebFragmentHelper.class);
        webItemFactory = new WebItemModuleDescriptorFactory(new WebItemModuleDescriptorFactoryForTests(webInterfaceManager), new IconModuleFragmentFactory(remotablePluginAccessorFactoryForTests), new ConditionModuleFragmentFactory(mock(ProductAccessor.class),remotablePluginAccessorFactoryForTests, new ParamsModuleFragmentFactory()));
        servletRequest = mock(HttpServletRequest.class);
        
        when(webInterfaceManager.getWebFragmentHelper()).thenReturn(webFragmentHelper);

        when(webFragmentHelper.renderVelocityFragment(anyString(),anyMap())).thenAnswer(
                new Answer<Object>() {
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
        WebItemCapabilityBean bean = newWebItemBean()
                .withName(new I18nProperty("My Web Item", "my.webitem"))
                .withLink("http://www.google.com")
                .withLocation("atl.admin/menu")
                .build();

        WebItemModuleProvider moduleProvier = new WebItemModuleProvider(webItemFactory, new RelativeAddOnUrlConverter(new UrlVariableSubstitutor()));
        
        List<ModuleDescriptor> descriptors = moduleProvier.provideModules(plugin, mock(BundleContext.class), JSON_FIELD_NAME, newArrayList(bean));
        
        assertEquals(1,descriptors.size());

        WebItemModuleDescriptor descriptor = (WebItemModuleDescriptor) descriptors.get(0);
        descriptor.enabled();

        assertEquals("http://www.google.com",descriptor.getLink().getDisplayableUrl(servletRequest, new HashMap<String, Object>()));
    }

    @Test
    public void singleAbsoluteLinkWithAddOnContext() throws Exception
    {
        WebItemCapabilityBean bean = newWebItemBean()
                .withName(new I18nProperty("My Web Item", "my.webitem"))
                .withLink("http://www.google.com")
                .withLocation("atl.admin/menu")
                .withContext(AddOnUrlContext.addon)
                .build();

        WebItemModuleProvider moduleProvier = new WebItemModuleProvider(webItemFactory, new RelativeAddOnUrlConverter(new UrlVariableSubstitutor()));

        List<ModuleDescriptor> descriptors = moduleProvier.provideModules(plugin, mock(BundleContext.class), JSON_FIELD_NAME, newArrayList(bean));

        assertEquals(1,descriptors.size());

        WebItemModuleDescriptor descriptor = (WebItemModuleDescriptor) descriptors.get(0);
        descriptor.enabled();

        assertEquals("http://www.google.com",descriptor.getLink().getDisplayableUrl(servletRequest, new HashMap<String, Object>()));
    }

    @Test
    public void singleAbsoluteLinkWithProductContext() throws Exception
    {
        WebItemCapabilityBean bean = newWebItemBean()
                .withName(new I18nProperty("My Web Item", "my.webitem"))
                .withLink("http://www.google.com")
                .withLocation("atl.admin/menu")
                .withContext(AddOnUrlContext.product)
                .build();

        WebItemModuleProvider moduleProvier = new WebItemModuleProvider(webItemFactory, new RelativeAddOnUrlConverter(new UrlVariableSubstitutor()));

        List<ModuleDescriptor> descriptors = moduleProvier.provideModules(plugin, mock(BundleContext.class), JSON_FIELD_NAME, newArrayList(bean));

        assertEquals(1,descriptors.size());

        WebItemModuleDescriptor descriptor = (WebItemModuleDescriptor) descriptors.get(0);
        descriptor.enabled();

        assertEquals("http://www.google.com",descriptor.getLink().getDisplayableUrl(servletRequest, new HashMap<String, Object>()));
    }

    @Test
    public void singleAddOnLink() throws Exception
    {
        WebItemCapabilityBean bean = newWebItemBean()
                .withName(new I18nProperty("My Web Item", "my.webitem"))
                .withLink("/some/admin")
                .withLocation("atl.admin/menu")
                .build();

        WebItemModuleProvider moduleProvier = new WebItemModuleProvider(webItemFactory, new RelativeAddOnUrlConverter(new UrlVariableSubstitutor()));

        List<ModuleDescriptor> descriptors = moduleProvier.provideModules(plugin, mock(BundleContext.class), JSON_FIELD_NAME, newArrayList(bean));

        assertEquals(1,descriptors.size());

        WebItemModuleDescriptor descriptor = (WebItemModuleDescriptor) descriptors.get(0);
        descriptor.enabled();

        assertEquals("http://ondemand.com/jira/plugins/servlet/ac/my-key/some/admin",descriptor.getLink().getDisplayableUrl(servletRequest, new HashMap<String, Object>()));
    }

    @Test
    public void singleAddOnLinkWithAddOnContext() throws Exception
    {
        WebItemCapabilityBean bean = newWebItemBean()
                .withName(new I18nProperty("My Web Item", "my.webitem"))
                .withLink("/some/admin")
                .withLocation("atl.admin/menu")
                .withContext(AddOnUrlContext.addon)
                .build();

        WebItemModuleProvider moduleProvier = new WebItemModuleProvider(webItemFactory, new RelativeAddOnUrlConverter(new UrlVariableSubstitutor()));

        List<ModuleDescriptor> descriptors = moduleProvier.provideModules(plugin, mock(BundleContext.class), JSON_FIELD_NAME, newArrayList(bean));

        assertEquals(1,descriptors.size());

        WebItemModuleDescriptor descriptor = (WebItemModuleDescriptor) descriptors.get(0);
        descriptor.enabled();

        assertURIEquals("http://ondemand.com/jira/plugins/servlet/ac/my-key/some/admin", descriptor.getLink().getDisplayableUrl(servletRequest, new HashMap<String, Object>()));
    }
    
    @Test
    public void singleProductLink() throws Exception
    {
        WebItemCapabilityBean bean = newWebItemBean()
                .withName(new I18nProperty("My Web Item", "my.webitem"))
                .withLink("/local/jira/admin")
                .withLocation("atl.admin/menu")
                .withContext(AddOnUrlContext.product)
                .build();

        WebItemModuleProvider moduleProvier = new WebItemModuleProvider(webItemFactory, new RelativeAddOnUrlConverter(new UrlVariableSubstitutor()));

        List<ModuleDescriptor> descriptors = moduleProvier.provideModules(plugin, mock(BundleContext.class), JSON_FIELD_NAME, newArrayList(bean));

        assertEquals(1,descriptors.size());

        WebItemModuleDescriptor descriptor = (WebItemModuleDescriptor) descriptors.get(0);
        descriptor.enabled();

        assertEquals("http://ondemand.com/jira/local/jira/admin",descriptor.getLink().getDisplayableUrl(servletRequest, new HashMap<String, Object>()));
    }

    @Test
    public void multipleWebItems() throws Exception
    {
        WebItemCapabilityBean bean = newWebItemBean()
                .withName(new I18nProperty("My Web Item", "my.webitem"))
                .withLink("http://www.google.com")
                .withLocation("atl.admin/menu")
                .build();

        WebItemCapabilityBean bean2 = newWebItemBean()
                .withName(new I18nProperty("My Other Web Item", "my.other.webitem"))
                .withLink("/my/addon")
                .withLocation("atl.admin/menu")
                .build();

        WebItemModuleProvider moduleProvier = new WebItemModuleProvider(webItemFactory, new RelativeAddOnUrlConverter(new UrlVariableSubstitutor()));

        List<ModuleDescriptor> descriptors = moduleProvier.provideModules(plugin, mock(BundleContext.class), JSON_FIELD_NAME, newArrayList(bean,bean2));

        assertEquals(2,descriptors.size());

        WebItemModuleDescriptor descriptor = (WebItemModuleDescriptor) descriptors.get(0);
        descriptor.enabled();

        WebItemModuleDescriptor descriptor2 = (WebItemModuleDescriptor) descriptors.get(1);
        descriptor2.enabled();

        assertEquals("http://www.google.com",descriptor.getLink().getDisplayableUrl(servletRequest, new HashMap<String, Object>()));
        assertEquals("http://ondemand.com/jira/plugins/servlet/ac/my-key/my/addon",descriptor2.getLink().getDisplayableUrl(servletRequest, new HashMap<String, Object>()));
    }
    
}
