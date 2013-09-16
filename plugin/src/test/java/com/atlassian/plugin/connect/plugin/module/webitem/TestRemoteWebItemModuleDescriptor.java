package com.atlassian.plugin.connect.plugin.module.webitem;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.integration.plugins.DynamicDescriptorRegistration;
import com.atlassian.plugin.connect.plugin.module.ConditionProcessor;
import com.atlassian.plugin.connect.plugin.module.IFrameRendererImpl;
import com.atlassian.plugin.connect.plugin.module.WebItemCreator;
import com.atlassian.plugin.connect.plugin.module.page.RemotePageDescriptorCreator;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlValidator;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessor;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.servlet.ServletModuleManager;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.sal.api.user.UserManager;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import java.net.URI;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TestRemoteWebItemModuleDescriptor
{
    @Mock ModuleFactory moduleFactory;
    @Mock DynamicDescriptorRegistration dynamicDescriptorRegistration;
    @Mock ConditionProcessor conditionProcessor;
    @Mock BundleContext bundleContext;
    @Mock UserManager userManager;
    IFrameRendererImpl iFrameRenderer = null;
    @Mock ProductAccessor productAccessor;
    @Mock UrlValidator urlValidator;
    @Mock RemotablePluginAccessorFactory pluginAccessorFactory;

    RemoteWebItemModuleDescriptor descriptor;

    @Before
    public void before()
    {
        when(bundleContext.getServiceReference(ServletModuleManager.class.getName())).thenReturn(mock(ServiceReference.class));
        when(bundleContext.getService(any(ServiceReference.class))).thenReturn(mock(ServletModuleManager.class));
        MyWebItemModuleDescriptorFactory webItemModuleDescriptorFactory = new MyWebItemModuleDescriptorFactory();
        WebItemCreator webItemCreator = new WebItemCreator(conditionProcessor, webItemModuleDescriptorFactory);
        IFrameRendererImpl iFrameRenderer = null;
        UrlVariableSubstitutor urlVariableSubstitutor = new UrlVariableSubstitutor();
        RemotePageDescriptorCreator remotePageDescriptorCreator = new RemotePageDescriptorCreator(bundleContext, userManager, webItemCreator, iFrameRenderer, productAccessor, urlValidator, urlVariableSubstitutor);
        descriptor = new RemoteWebItemModuleDescriptor(moduleFactory, dynamicDescriptorRegistration, remotePageDescriptorCreator,
                urlValidator, conditionProcessor, webItemCreator, urlVariableSubstitutor, pluginAccessorFactory);
        RemotablePluginAccessor remotablePluginAccessor = mock(RemotablePluginAccessor.class);
        when(remotablePluginAccessor.getDisplayUrl()).thenReturn(URI.create("mock"));
        when(pluginAccessorFactory.get(any(String.class))).thenReturn(remotablePluginAccessor);

        descriptor.init(mock(Plugin.class), createDescriptorElement());
        descriptor.enabled();
    }

    @After
    public void after()
    {
        MyWebItemModuleDescriptor.link = null;
        MyWebItemModuleDescriptorFactory.url = null;
    }

    private Element createDescriptorElement()
    {
        DefaultElement descriptorElement = new DefaultElement("descriptor");
        descriptorElement.addAttribute("name", "descriptor");
        descriptorElement.addAttribute("key", "module-key");

        DefaultElement linkElement = (DefaultElement) descriptorElement.addElement("link");
        linkElement.setText("my_page_id=${page.id}");

        return descriptorElement;
    }

    @Test
    public void urlHasBeenSet()
    {
        assertThat(MyWebItemModuleDescriptorFactory.url, is(not(nullValue())));
    }

    @Test
    public void linkTextHasBeenSet()
    {
        assertThat(MyWebItemModuleDescriptor.link, is(not(nullValue())));
    }

    @Test
    public void urlIsCorrect()
    {
        assertThat(MyWebItemModuleDescriptorFactory.url, is("/plugins/servlet/atlassian-connect/null/module-key?my_page_id=1234"));
    }

    @Test
    public void linkTestIsConsistent()
    {
        assertThat(MyWebItemModuleDescriptor.link, is(MyWebItemModuleDescriptorFactory.url));
    }

    private static abstract class MyWebItemModuleDescriptor implements WebItemModuleDescriptor
    {
        static String link = null;

        @Override
        public void init(Plugin plugin, Element element)
        {
            Element link = element.element("link");

            if (null != link)
            {
                if (null != MyWebItemModuleDescriptor.link && !MyWebItemModuleDescriptor.link.equals(link.getStringValue()))
                {
                    throw new RuntimeException("MyWebItemModuleDescriptor.link should be set to exactly one value");
                }

                MyWebItemModuleDescriptor.link = element.getStringValue();
            }
        }
    }

    private static class MyWebItemModuleDescriptorFactory implements WebItemModuleDescriptorFactory
    {
        static String url = null;

        @Override
        public WebItemModuleDescriptor createWebItemModuleDescriptor(String url, String moduleKey, boolean absolute)
        {
            if (null != MyWebItemModuleDescriptorFactory.url && !MyWebItemModuleDescriptorFactory.url.equals(url))
            {
                throw new RuntimeException("MyWebItemModuleDescriptorFactory.url should be set to exactly one value");
            }

            MyWebItemModuleDescriptorFactory.url = url;
            MyWebItemModuleDescriptor webItemModuleDescriptor = mock(MyWebItemModuleDescriptor.class, CALLS_REAL_METHODS);
            return webItemModuleDescriptor;
        }
    }
}
