package com.atlassian.plugin.connect.plugin.web.panel;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.api.util.ConnectContainerUtil;
import com.atlassian.plugin.connect.api.web.condition.ConditionModuleFragmentFactory;
import com.atlassian.plugin.connect.api.web.item.ModuleLocationQualifier;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.WebPanelModuleBeanBuilder;
import com.atlassian.plugin.connect.spi.lifecycle.ConnectModuleProviderContext;
import com.atlassian.plugin.connect.spi.web.ProductWebPanelElementEnhancer;
import com.atlassian.plugin.connect.spi.web.panel.WebPanelLocationValidator;
import org.dom4j.Element;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;

@RunWith (MockitoJUnitRunner.class)
public class WebPanelConnectModuleDescriptorFactoryTest
{
    @Mock
    private ConnectContainerUtil connectContainerUtil;
    @Mock
    private ConditionModuleFragmentFactory conditionModuleFragmentFactory;
    @Mock
    private WebPanelLocationValidator webPanelLocationValidator;

    private WebPanelConnectModuleDescriptorFactory descriptorFactory;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception
    {
        descriptorFactory = new WebPanelConnectModuleDescriptorFactory(connectContainerUtil, conditionModuleFragmentFactory, webPanelLocationValidator);
        when(connectContainerUtil.getBeansOfType(ProductWebPanelElementEnhancer.class)).thenReturn(Collections.<ProductWebPanelElementEnhancer>emptyList());
        when(webPanelLocationValidator.validateLocation(anyString())).thenReturn(true);
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

    @Test
    public void webPanelModuleDescriptionCreationFailsWhenLocationValidationFails()
    {
        String illegalLocation = "bad-location";

        WebPanelModuleBean bean = new WebPanelModuleBeanBuilder()
                .withKey("nonemptykey")
                .withLocation(illegalLocation)
                .build();

        when(webPanelLocationValidator.validateLocation(illegalLocation)).thenReturn(false);

        expectedException.expect(PluginParseException.class);
        descriptorFactory.createModuleDescriptor(mockProviderContextWithLocation(illegalLocation), mock(Plugin.class), bean);

        verify(webPanelLocationValidator).validateLocation(illegalLocation);
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
