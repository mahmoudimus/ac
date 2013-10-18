package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectVersionTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectAutowireUtil;
import com.atlassian.plugin.module.ContainerManagedPlugin;
import org.dom4j.Element;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectVersionTabPanelCapabilityBean.newVersionTabPageBean;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class ConnectVersionTabPanelModuleDescriptorFactoryTest
{
    private ConnectVersionTabPanelModuleDescriptorFactory versionTabPageFactory;

    @Mock
    private ContainerManagedPlugin plugin;

    @Mock
    private ConnectVersionTabPanelModuleDescriptor connectVersionTabPanelModuleDescriptor;

    @Mock
    private ConnectAutowireUtil connectAutowireUtil;

    @Before
    public void setup()
    {
        when(plugin.getKey()).thenReturn("my-key");
        when(plugin.getName()).thenReturn("My Plugin");

        when(connectAutowireUtil.createBean(ConnectVersionTabPanelModuleDescriptor.class)).thenReturn(connectVersionTabPanelModuleDescriptor);

        versionTabPageFactory = new ConnectVersionTabPanelModuleDescriptorFactory(connectAutowireUtil);
    }

    @Test
    public void simpleDescriptorCreation() throws Exception
    {
        ConnectVersionTabPanelCapabilityBean bean = newVersionTabPageBean()
                .withName(new I18nProperty("My Version Tab Page", "my.versiontabpage"))
                .withUrl("http://www.google.com")
                .withWeight(99)
                .build();

        ConnectVersionTabPanelModuleDescriptor descriptor = versionTabPageFactory.createModuleDescriptor(plugin, mock(BundleContext.class), bean);
        ArgumentCaptor<Element> agumentCaptor = ArgumentCaptor.forClass(Element.class);
        verify(connectVersionTabPanelModuleDescriptor, times(1)).init(eq(plugin), agumentCaptor.capture());

        Element versionTabPageElement = agumentCaptor.getValue();

        assertThat(versionTabPageElement.attributeValue("key"), is(equalTo("my-version-tab-page")));
        assertThat(versionTabPageElement.element("order").getText(), is(equalTo("99")));
        assertThat(versionTabPageElement.attributeValue("url"), is(equalTo("http://www.google.com")));
        Element label = versionTabPageElement.element("label");
        assertThat(label.attributeValue("key"), is(equalTo("my.versiontabpage")));
        assertThat(label.getText(), is(equalTo("My Version Tab Page")));
        assertThat(versionTabPageElement.attributeValue("name"), is(equalTo("My Version Tab Page")));
    }
}
