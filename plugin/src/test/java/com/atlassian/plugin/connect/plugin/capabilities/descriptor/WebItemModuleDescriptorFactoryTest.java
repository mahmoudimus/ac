package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.testobjects.PluginForTests;
import com.atlassian.plugin.connect.plugin.capabilities.testobjects.RemotablePluginAccessorFactoryForTests;
import com.atlassian.plugin.connect.plugin.capabilities.testobjects.descriptor.WebItemModuleDescriptorFactoryForTests;
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

import static com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemCapabilityBean.newWebItemBean;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since 1.0
 */
public class WebItemModuleDescriptorFactoryTest
{
    Plugin plugin;
    WebInterfaceManager webInterfaceManager;
    WebFragmentHelper webFragmentHelper;
    WebItemModuleDescriptorFactory webItemFactory;
    HttpServletRequest servletRequest;
    
    @Before
    public void setup()
    {
        plugin = new PluginForTests("my-key", "My Plugin");

        RemotablePluginAccessorFactoryForTests remotablePluginAccessorFactoryForTests = new RemotablePluginAccessorFactoryForTests();
        ConditionModuleFragmentFactory conditionModuleFragmentFactory = new ConditionModuleFragmentFactory(mock(ProductAccessor.class),remotablePluginAccessorFactoryForTests);
        
        webInterfaceManager = mock(WebInterfaceManager.class);
        webFragmentHelper = mock(WebFragmentHelper.class);
        webItemFactory = new WebItemModuleDescriptorFactory(new WebItemModuleDescriptorFactoryForTests(webInterfaceManager), new IconModuleFragmentFactory(new RemotablePluginAccessorFactoryForTests()), conditionModuleFragmentFactory);
        servletRequest = mock(HttpServletRequest.class);

        when(servletRequest.getContextPath()).thenReturn("http://ondemand.com/jira");
        
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
    }
    
    @Test
    public void simpleDescriptorCreation() throws Exception
    {
        
        when(webFragmentHelper.loadCondition(anyString(), any(Plugin.class))).thenReturn(new DynamicMarkerCondition());
        
        WebItemCapabilityBean bean = newWebItemBean()
                .withName(new I18nProperty("My Web Item", "my.webitem"))
                .withLink("http://www.google.com")
                .withLocation("atl.admin/menu")
                .build();

        WebItemModuleDescriptor descriptor = webItemFactory.createModuleDescriptor(plugin, mock(BundleContext.class), bean);
        descriptor.enabled();
        
        assertEquals("my-key:my-web-item", descriptor.getCompleteKey());
        assertEquals("atl.admin/menu", descriptor.getSection());
        assertEquals("http://www.google.com", descriptor.getLink().getDisplayableUrl(mock(HttpServletRequest.class), new HashMap<String, Object>()));
        assertEquals(100, descriptor.getWeight());
        assertNull(descriptor.getIcon());
        assertEquals("",descriptor.getStyleClass());
    }
}
