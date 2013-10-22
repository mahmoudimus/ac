package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.connect.plugin.capabilities.beans.AbstractConnectTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectAutowireUtil;
import com.atlassian.plugin.connect.plugin.util.matchers.ElementAttributeParamMatcher;
import com.atlassian.plugin.connect.plugin.util.matchers.ElementSubElementTextMatcher;
import com.atlassian.plugin.connect.plugin.util.matchers.SubElementParamMatcher;
import com.atlassian.plugin.module.ContainerManagedPlugin;
import org.dom4j.Element;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;

import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractConnectTabPanelModuleDescriptorFactoryTest
{
    private final Class<? extends ModuleDescriptor<?>> descriptorClass;
    private final Class<?> moduleClass;
    private AbstractConnectTabPanelModuleDescriptorFactory tabPanelModuleDescriptorFactory;

    @Mock
    private ContainerManagedPlugin plugin;


    private ModuleDescriptor connectTabPanelModuleDescriptor;

    @Mock
    private ConnectAutowireUtil connectAutowireUtil;

    protected AbstractConnectTabPanelModuleDescriptorFactoryTest(Class<? extends ModuleDescriptor<?>> descriptorClass, Class<?> moduleClass)
    {
        this.descriptorClass = descriptorClass;
        this.moduleClass = moduleClass;
    }

    @Before
    public void setup()
    {
        connectTabPanelModuleDescriptor = mock(descriptorClass);

        when(plugin.getKey()).thenReturn("my-key");
        when(plugin.getName()).thenReturn("My Plugin");

        when(connectAutowireUtil.createBean(descriptorClass)).thenReturn(connectTabPanelModuleDescriptor);

        tabPanelModuleDescriptorFactory = createDescriptorFactory(connectAutowireUtil);
        createCapabilityModuleDescriptor();
    }

    protected abstract AbstractConnectTabPanelModuleDescriptorFactory createDescriptorFactory(ConnectAutowireUtil connectAutowireUtil);

    protected abstract AbstractConnectTabPanelCapabilityBean createCapabilityBean();

    private AbstractConnectTabPanelCapabilityBean createCapabilityModuleDescriptor()
    {
        AbstractConnectTabPanelCapabilityBean bean = createCapabilityBean();
        tabPanelModuleDescriptorFactory.createModuleDescriptor(plugin, mock(BundleContext.class), bean);
        return bean;
    }

    @Test
    public void createsElementWithCorrectKey()
    {
        verify(connectTabPanelModuleDescriptor, times(1)).init(eq(plugin), argThat(hasElementKey("my-tab-page")));
    }

    @Test
    public void createsElementWithCorrectName()
    {
        verify(connectTabPanelModuleDescriptor, times(1)).init(eq(plugin), argThat(hasElementName("My Tab Page")));
    }

    @Test
    public void createsElementWithCorrectUrl()
    {
        verify(connectTabPanelModuleDescriptor, times(1)).init(eq(plugin), argThat(hasElementUrl("http://www.google.com")));
    }

    @Test
    public void createsElementWithCorrectClass()
    {
        verify(connectTabPanelModuleDescriptor, times(1)).init(eq(plugin), argThat(hasElementClass(moduleClass.getName())));
    }

    @Test
    public void createsElementWithCorrectOrder()
    {
        verify(connectTabPanelModuleDescriptor, times(1)).init(eq(plugin), argThat(hasElementOrder("99")));
    }

    @Test
    public void createsElementWithCorrectLabelText()
    {
        verify(connectTabPanelModuleDescriptor, times(1)).init(eq(plugin), argThat(hasElementLabelText("My Tab Page")));
    }

    @Test
    public void createsElementWithCorrectLabelKey()
    {
        verify(connectTabPanelModuleDescriptor, times(1)).init(eq(plugin), argThat(hasElementLabelKey("my.tabpage")));
    }



    private static ArgumentMatcher<Element> hasElementKey(String expectedValue)
    {
        return hasAttributeValue("key", expectedValue);
    }

    private static ArgumentMatcher<Element> hasElementUrl(String expectedValue)
    {
        return hasAttributeValue("url", expectedValue);
    }

    private static ArgumentMatcher<Element> hasElementName(String expectedValue)
    {
        return hasAttributeValue("name", expectedValue);
    }

    private static ArgumentMatcher<Element> hasElementClass(String expectedValue)
    {
        return hasAttributeValue("class", expectedValue);
    }

    private static ArgumentMatcher<Element> hasElementOrder(String expectedValue)
    {
        return hasSubElementTextValue("order", expectedValue);
    }

    private static ArgumentMatcher<Element> hasElementLabelText(String expectedValue)
    {
        return hasSubElementTextValue("label", expectedValue);
    }

    private static ArgumentMatcher<Element> hasElementLabelKey(String expectedValue)
    {
        return hasSubElementAttributeValue("label", "key", expectedValue);
    }


    private static ArgumentMatcher<Element> hasAttributeValue(String name, String expectedValue)
    {
        return new ElementAttributeParamMatcher(name, expectedValue);
    }

    private static ArgumentMatcher<Element> hasSubElementAttributeValue(String subElementName, String attributeName, String expectedValue)
    {
        return new SubElementParamMatcher(subElementName, new ElementAttributeParamMatcher(attributeName, expectedValue));
    }


    private static ArgumentMatcher<Element> hasSubElementTextValue(String name, String expectedValue)
    {
        return new ElementSubElementTextMatcher(name, expectedValue);
    }


}
