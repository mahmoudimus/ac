package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.jira.plugin.projectpanel.ProjectTabPanel;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectProjectTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
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
public class ConnectProjectTabPanelModuleDescriptorFactoryTest
{
    private ConnectProjectTabPanelModuleDescriptorFactory projectTabPageFactory;

    @Mock
    private ContainerManagedPlugin plugin;

    @Mock
    private ConnectProjectTabPanelModuleDescriptor connectProjectTabPanelModuleDescriptor;

    @Mock
    private ConnectAutowireUtil connectAutowireUtil;

    @Before
    public void setup()
    {
        when(plugin.getKey()).thenReturn("my-key");
        when(plugin.getName()).thenReturn("My Plugin");

        when(connectAutowireUtil.createBean(ConnectProjectTabPanelModuleDescriptor.class)).thenReturn(connectProjectTabPanelModuleDescriptor);

        projectTabPageFactory = new ConnectProjectTabPanelModuleDescriptorFactory(connectAutowireUtil);
        createCapabilityModuleDescriptor();
    }

    private ConnectProjectTabPanelCapabilityBean createCapabilityModuleDescriptor()
    {
        ConnectProjectTabPanelCapabilityBean bean = ConnectProjectTabPanelCapabilityBean.newProjectTabPanelBean()
                .withName(new I18nProperty("My Project Tab Page", "my.projecttabpage"))
                .withUrl("http://www.google.com")
                .withWeight(99)
                .build();
        projectTabPageFactory.createModuleDescriptor(plugin, mock(BundleContext.class), bean);
        return bean;
    }

    @Test
    public void createsElementWithCorrectKey()
    {
        verify(connectProjectTabPanelModuleDescriptor, times(1)).init(eq(plugin), argThat(hasElementKey("my-project-tab-page")));
    }

    @Test
    public void createsElementWithCorrectName()
    {
        verify(connectProjectTabPanelModuleDescriptor, times(1)).init(eq(plugin), argThat(hasElementName("My Project Tab Page")));
    }

    @Test
    public void createsElementWithCorrectUrl()
    {
        verify(connectProjectTabPanelModuleDescriptor, times(1)).init(eq(plugin), argThat(hasElementUrl("http://www.google.com")));
    }

    @Test
    public void createsElementWithCorrectClass()
    {
        verify(connectProjectTabPanelModuleDescriptor, times(1)).init(eq(plugin), argThat(hasElementClass(ProjectTabPanel.class.getName())));
    }

    @Test
    public void createsElementWithCorrectOrder()
    {
        verify(connectProjectTabPanelModuleDescriptor, times(1)).init(eq(plugin), argThat(hasElementOrder("99")));
    }

    @Test
    public void createsElementWithCorrectLabelText()
    {
        verify(connectProjectTabPanelModuleDescriptor, times(1)).init(eq(plugin), argThat(hasElementLabelText("My Project Tab Page")));
    }

    @Test
    public void createsElementWithCorrectLabelKey()
    {
        verify(connectProjectTabPanelModuleDescriptor, times(1)).init(eq(plugin), argThat(hasElementLabelKey("my.projecttabpage")));
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
//

    private static ArgumentMatcher<Element> hasSubElementTextValue(String name, String expectedValue)
    {
        return new ElementSubElementTextMatcher(name, expectedValue);
    }

}