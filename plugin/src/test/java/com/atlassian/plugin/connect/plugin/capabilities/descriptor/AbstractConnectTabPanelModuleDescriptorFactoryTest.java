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
    private static final String ADDON_KEY = "my-key";
    private static final String ADDON_NAME = "My Plugin";
    private static final String ADDON_NAME_KEY = "my-tab-page";
    private static final String ADDON_MODULE_NAME = "My Tab Page";
    private static final String ADDON_URL = "http://www.google.com";
    private static final int ADDON_WEIGHT = 99;
    private static final String ADDON_WEIGHT_STR = Integer.toString(ADDON_WEIGHT);
    private static final String ADDON_LABEL_KEY = "my.tabpage";
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

        when(plugin.getKey()).thenReturn(ADDON_KEY);
        when(plugin.getName()).thenReturn(ADDON_NAME);

        when(connectAutowireUtil.createBean(descriptorClass)).thenReturn(connectTabPanelModuleDescriptor);

        tabPanelModuleDescriptorFactory = createDescriptorFactory(connectAutowireUtil);
        createCapabilityModuleDescriptor();
    }

    protected abstract AbstractConnectTabPanelModuleDescriptorFactory createDescriptorFactory(ConnectAutowireUtil connectAutowireUtil);

    protected abstract AbstractConnectTabPanelCapabilityBean createCapabilityBean(String name, String i18NameKey, String url, int weight);

    private AbstractConnectTabPanelCapabilityBean createCapabilityModuleDescriptor()
    {
        AbstractConnectTabPanelCapabilityBean bean = createCapabilityBean(ADDON_MODULE_NAME, ADDON_LABEL_KEY, ADDON_URL, ADDON_WEIGHT);
        tabPanelModuleDescriptorFactory.createModuleDescriptor(plugin, mock(BundleContext.class), bean);
        return bean;
    }

    @Test
    public void createsElementWithCorrectKey()
    {
        verify(connectTabPanelModuleDescriptor, times(1)).init(eq(plugin), argThat(hasElementKey(ADDON_NAME_KEY)));
    }

    @Test
    public void createsElementWithCorrectName()
    {
        verify(connectTabPanelModuleDescriptor, times(1)).init(eq(plugin), argThat(hasElementName(ADDON_MODULE_NAME)));
    }

    @Test
    public void createsElementWithCorrectUrl()
    {
        verify(connectTabPanelModuleDescriptor, times(1)).init(eq(plugin), argThat(hasElementUrl(ADDON_URL)));
    }

    @Test
    public void createsElementWithCorrectClass()
    {
        verify(connectTabPanelModuleDescriptor, times(1)).init(eq(plugin), argThat(hasElementClass(moduleClass.getName())));
    }

    @Test
    public void createsElementWithCorrectOrder()
    {
        verify(connectTabPanelModuleDescriptor, times(1)).init(eq(plugin), argThat(hasElementOrder(ADDON_WEIGHT_STR)));
    }

    @Test
    public void createsElementWithCorrectLabelText()
    {
        verify(connectTabPanelModuleDescriptor, times(1)).init(eq(plugin), argThat(hasElementLabelText(ADDON_MODULE_NAME)));
    }

    @Test
    public void createsElementWithCorrectLabelKey()
    {
        verify(connectTabPanelModuleDescriptor, times(1)).init(eq(plugin), argThat(hasElementLabelKey(ADDON_LABEL_KEY)));
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
