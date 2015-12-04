package com.atlassian.plugin.connect.plugin.web.panel;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.util.ConnectContainerUtil;
import com.atlassian.plugin.connect.api.web.WebFragmentLocationQualifier;
import com.atlassian.plugin.connect.api.web.condition.ConditionModuleFragmentFactory;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.WebPanelModuleBeanBuilder;
import com.atlassian.plugin.connect.spi.web.panel.ProductWebPanelElementEnhancer;
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
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WebPanelConnectModuleDescriptorFactoryTest
{

    @Mock
    private ConnectContainerUtil connectContainerUtil;

    @Mock
    private WebFragmentLocationQualifier webFragmentLocationQualifier;

    @Mock
    private ConditionModuleFragmentFactory conditionModuleFragmentFactory;

    @Mock
    private ConnectAddonBean addon;

    @Mock
    private Plugin plugin;

    @Mock
    private WebPanelConnectModuleDescriptor descriptor;

    private WebPanelConnectModuleDescriptorFactory descriptorFactory;

    @Before
    public void setUp() throws Exception
    {
        descriptorFactory = new WebPanelConnectModuleDescriptorFactory(connectContainerUtil,
                webFragmentLocationQualifier, conditionModuleFragmentFactory);
        when(connectContainerUtil.createBean(WebPanelConnectModuleDescriptor.class)).thenReturn(descriptor);
        when(connectContainerUtil.getBeansOfType(ProductWebPanelElementEnhancer.class)).thenReturn(Collections.<ProductWebPanelElementEnhancer>emptyList());
    }

    @Test
    public void webPanelModuleDescriptorHasCorrectLocation() throws Exception
    {
        String location = "location";
        String expectedLocation = "somelocation";
        when(webFragmentLocationQualifier.processLocation(location, addon)).thenReturn(expectedLocation);

        WebPanelModuleBean webPanel = new WebPanelModuleBeanBuilder().withKey("some-key").withLocation(location).build();
        descriptorFactory.createModuleDescriptor(webPanel, addon, plugin);

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
