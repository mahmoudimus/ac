package com.atlassian.plugin.connect.plugin.module.webitem;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.plugin.integration.plugins.DynamicDescriptorRegistration;
import com.atlassian.plugin.connect.plugin.module.ConditionProcessor;
import com.atlassian.plugin.connect.plugin.module.IFramePageRenderer;
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
import org.mockito.Mock;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import java.net.URI;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class RemoteWebItemModuleDescriptorTestBase
{
    protected abstract String getExpectedUrl();
    protected abstract String getInputLinkText(); // as appears in atlassian-plugin.xml

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
        assertThat(MyWebItemModuleDescriptorFactory.url, is(getExpectedUrl()));
    }

    @Test
    public void linkTextAndUrlAreConsistent()
    {
        assertThat(MyWebItemModuleDescriptorFactory.url, endsWith(MyWebItemModuleDescriptor.link));
    }

    @Mock ModuleFactory moduleFactory;
    @Mock DynamicDescriptorRegistration dynamicDescriptorRegistration;
    @Mock ConditionProcessor conditionProcessor;
    @Mock BundleContext bundleContext;
    @Mock UserManager userManager;
    @Mock ProductAccessor productAccessor;
    @Mock UrlValidator urlValidator;
    @Mock RemotablePluginAccessorFactory pluginAccessorFactory;

    @Before
    public void before()
    {
        when(bundleContext.getServiceReference(ServletModuleManager.class.getName())).thenReturn(mock(ServiceReference.class));
        when(bundleContext.getService(any(ServiceReference.class))).thenReturn(mock(ServletModuleManager.class));
        MyWebItemModuleDescriptorFactory webItemModuleDescriptorFactory = new MyWebItemModuleDescriptorFactory();
        WebItemCreator webItemCreator = new WebItemCreator(conditionProcessor, webItemModuleDescriptorFactory);
        IFramePageRenderer iFramePageRenderer = null;
        UrlVariableSubstitutor urlVariableSubstitutor = new UrlVariableSubstitutor();
        RemotePageDescriptorCreator remotePageDescriptorCreator = new RemotePageDescriptorCreator(bundleContext, userManager, webItemCreator, iFramePageRenderer, productAccessor, urlValidator, urlVariableSubstitutor);
        RemoteWebItemModuleDescriptor descriptor = new RemoteWebItemModuleDescriptor(moduleFactory, dynamicDescriptorRegistration, remotePageDescriptorCreator,
                urlValidator, conditionProcessor, webItemCreator, urlVariableSubstitutor, pluginAccessorFactory);
        RemotablePluginAccessor remotablePluginAccessor = mock(RemotablePluginAccessor.class);

        when(remotablePluginAccessor.getBaseUrl()).thenReturn(URI.create("mock"));
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
        linkElement.setText(getInputLinkText());

        return descriptorElement;
    }

    protected static abstract class MyWebItemModuleDescriptor implements WebItemModuleDescriptor
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

                MyWebItemModuleDescriptor.link = link.getStringValue();
            }
        }
    }

    protected static class MyWebItemModuleDescriptorFactory implements ProductSpecificWebItemModuleDescriptorFactory
    {
        static String url = null;

        @Override
        public WebItemModuleDescriptor createWebItemModuleDescriptor(String url, String moduleKey, boolean absolute,
                                                                     AddOnUrlContext addOnUrlContext)
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
