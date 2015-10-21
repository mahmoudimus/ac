package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.capabilities.descriptor.ConditionModuleFragmentFactory;
import com.atlassian.plugin.connect.api.capabilities.util.ConnectContainerUtil;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.WebPanelModuleBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.webpanel.WebPanelConnectModuleDescriptor;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.webpanel.WebPanelConnectModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.module.ModuleLocationQualifier;
import com.atlassian.plugin.connect.spi.module.ConnectModuleProviderContext;
import com.atlassian.plugin.connect.spi.web.ProductWebPanelElementEnhancer;
import org.dom4j.Element;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class WebPanelConnectModuleDescriptorFactoryTest
{
    @Mock
    private ConnectContainerUtil connectContainerUtil;
    @Mock
    private ConditionModuleFragmentFactory conditionModuleFragmentFactory;


    private WebPanelConnectModuleDescriptorFactory descriptorFactory;

    @Before
    public void setUp() throws Exception
    {
        descriptorFactory = new WebPanelConnectModuleDescriptorFactory(connectContainerUtil, conditionModuleFragmentFactory);
        when(connectContainerUtil.getBeansOfType(ProductWebPanelElementEnhancer.class)).thenReturn(Collections.<ProductWebPanelElementEnhancer>emptyList());
    }

    private ConnectModuleProviderContext mockProviderContextWithLocation(final String location)
    {
        ConnectModuleProviderContext providerContext = mock(ConnectModuleProviderContext.class);

        ConnectAddonBean addonBean = mock(ConnectAddonBean.class);
        when(providerContext.getConnectAddonBean()).thenReturn(addonBean);

        ModuleLocationQualifier locationQualifier = mock(ModuleLocationQualifier.class);
        when(providerContext.getLocationQualifier()).thenReturn(locationQualifier);
        when(locationQualifier.processLocation(anyString())).thenReturn(location);

        return providerContext;
    }

    @Test
    public void webPanelModuleDescriptorHasCorrectLocation() throws Exception
    {

        WebPanelModuleBean bean = new WebPanelModuleBeanBuilder().withKey("nonemptykey").build();

        String expectedLocation = "somelocation";

        ConnectModuleProviderContext providerContext = mockProviderContextWithLocation(expectedLocation);

        WebPanelConnectModuleDescriptor descriptor = mock(WebPanelConnectModuleDescriptor.class);
        when(connectContainerUtil.createBean(WebPanelConnectModuleDescriptor.class)).thenReturn(descriptor);

        descriptorFactory.createModuleDescriptor(providerContext, mock(Plugin.class), bean);

        verify(descriptor).init(any(Plugin.class), argThat(hasLocationAttributeEqualTo(expectedLocation)));
    }

    private Matcher<Element> hasLocationAttributeEqualTo(final String value)
    {
        return new TypeSafeMatcher<Element>()
        {
            @Override
            protected boolean matchesSafely(final Element element)
            {
                return getLocationAttributeValue(element).equals(value);
            }

            private Object getLocationAttributeValue(final Element element)
            {
                return element.attribute("location").getData();
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("Element with location with value: ").appendValue(value);
            }
        };
    }
}
