package com.atlassian.plugin.connect.test.plugin.capabilities.descriptor;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConditionModuleFragmentFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.tabpanel.ConnectTabPanelModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.provider.TabPanelDescriptorHints;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectContainerUtil;
import com.atlassian.plugin.connect.test.plugin.util.matchers.dom4j.Dom4JElementMatchers;
import com.atlassian.plugin.module.ContainerManagedPlugin;

import org.dom4j.Element;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean.newTabPanelBean;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractConnectTabPanelModuleDescriptorFactoryTest
{
    private static final String ADDON_KEY = "my-key";
    private static final String ADDON_NAME = "My Plugin";
    private static final String ADDON_NAME_KEY = "my-tab-page";
    private static final String ADDON_MODULE_NAME = "My Tab Page";
    private static final String ADDON_MODULE_KEY = "my-tab-page";
    private static final String ADDON_URL = "http://www.google.com";
    private static final int ADDON_WEIGHT = 99;
    private static final String ADDON_WEIGHT_STR = Integer.toString(ADDON_WEIGHT);
    private static final String ADDON_LABEL_KEY = "my.tabpage";
    private final TabPanelDescriptorHints descriptorHints;
    private ConnectTabPanelModuleDescriptorFactory tabPanelModuleDescriptorFactory;


    @Mock
    private ContainerManagedPlugin plugin;
    
    private ConnectAddonBean addon;


    private ModuleDescriptor connectTabPanelModuleDescriptor;

    @Mock
    private ConnectContainerUtil connectContainerUtil;

    protected AbstractConnectTabPanelModuleDescriptorFactoryTest(TabPanelDescriptorHints descriptorHints)
    {
        this.descriptorHints = descriptorHints;
    }

    @Before
    public void setup()
    {
        this.addon = newConnectAddonBean().withKey(ADDON_KEY).withName(ADDON_NAME).build();
        
        when(plugin.getKey()).thenReturn(ADDON_KEY);
        when(plugin.getName()).thenReturn(ADDON_NAME);

        when(connectContainerUtil.createBean(any(Class.class))).thenAnswer(new Answer<Object>()
        {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable
            {
                return mock((Class) invocation.getArguments()[0]);
            }
        });

        tabPanelModuleDescriptorFactory = createDescriptorFactory(connectContainerUtil);
        createModuleDescriptor();
    }

    protected ConnectTabPanelModuleDescriptorFactory createDescriptorFactory(ConnectContainerUtil connectContainerUtil)
    {
        return new ConnectTabPanelModuleDescriptorFactory(mock(ConditionModuleFragmentFactory.class), connectContainerUtil);
    }

    protected ConnectTabPanelModuleBean createModuleBean(String name, String i18NameKey, String key, String url, int weight)
    {
        return newTabPanelBean()
                .withName(new I18nProperty(name, i18NameKey))
                .withKey(key)
                .withUrl(url)
                .withWeight(weight)
                .build();
    }

    private ConnectTabPanelModuleBean createModuleDescriptor()
    {
        ConnectTabPanelModuleBean bean = createModuleBean(ADDON_MODULE_NAME, ADDON_LABEL_KEY, ADDON_MODULE_KEY, ADDON_URL, ADDON_WEIGHT);
        connectTabPanelModuleDescriptor = tabPanelModuleDescriptorFactory.createModuleDescriptor(addon, plugin, bean, descriptorHints);
        return bean;
    }

    @Test
    public void createsElementWithCorrectKey()
    {
        verify(connectTabPanelModuleDescriptor, times(1)).init(eq(plugin), argThat(hasElementKey(ADDON_MODULE_KEY)));
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
        verify(connectTabPanelModuleDescriptor, times(1)).init(eq(plugin), argThat(hasElementClass(descriptorHints.getModuleClass().getName())));
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
        return Dom4JElementMatchers.hasAttributeValue("key", expectedValue);
    }

    private static ArgumentMatcher<Element> hasElementUrl(String expectedValue)
    {
        return Dom4JElementMatchers.hasAttributeValue("url", expectedValue);
    }

    private static ArgumentMatcher<Element> hasElementName(String expectedValue)
    {
        return Dom4JElementMatchers.hasAttributeValue("name", expectedValue);
    }

    private static ArgumentMatcher<Element> hasElementClass(String expectedValue)
    {
        return Dom4JElementMatchers.hasAttributeValue("class", expectedValue);
    }

    private static ArgumentMatcher<Element> hasElementOrder(String expectedValue)
    {
        return Dom4JElementMatchers.hasSubElementTextValue("order", expectedValue);
    }

    private static ArgumentMatcher<Element> hasElementLabelText(String expectedValue)
    {
        return Dom4JElementMatchers.hasSubElementTextValue("label", expectedValue);
    }

    private static ArgumentMatcher<Element> hasElementLabelKey(String expectedValue)
    {
        return Dom4JElementMatchers.hasSubElementAttributeValue("label", "key", expectedValue);
    }
}
