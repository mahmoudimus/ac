package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectIssueTabPanelCapabilityBean;
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

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectIssueTabPanelCapabilityBean.newIssueTabPageBean;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class ConnectIssueTabPanelModuleDescriptorFactoryTest
{
    private ConnectIssueTabPanelModuleDescriptorFactory issueTabPageFactory;

    @Mock
    private ContainerManagedPlugin plugin;

    @Mock
    private ConnectIssueTabPanelModuleDescriptor connectIssueTabPanelModuleDescriptor;

    @Mock
    private ConnectAutowireUtil connectAutowireUtil;

    @Before
    public void setup()
    {
        when(plugin.getKey()).thenReturn("my-key");
        when(plugin.getName()).thenReturn("My Plugin");

        when(connectAutowireUtil.createBean(ConnectIssueTabPanelModuleDescriptor.class)).thenReturn(connectIssueTabPanelModuleDescriptor);

        issueTabPageFactory = new ConnectIssueTabPanelModuleDescriptorFactory(connectAutowireUtil);
    }

    @Test
    public void simpleDescriptorCreation() throws Exception
    {
        ConnectIssueTabPanelCapabilityBean bean = newIssueTabPageBean()
                .withName(new I18nProperty("My Issue Tab Page", "my.issuetabpage"))
                .withUrl("http://www.google.com")
                .withWeight(99)
                .build();

        ConnectIssueTabPanelModuleDescriptor descriptor = issueTabPageFactory.createModuleDescriptor(plugin, mock(BundleContext.class), bean);
        ArgumentCaptor<Element> agumentCaptor = ArgumentCaptor.forClass(Element.class);
        verify(connectIssueTabPanelModuleDescriptor, times(1)).init(eq(plugin), agumentCaptor.capture());

        Element issueTabPageElement = agumentCaptor.getValue();

        assertThat(issueTabPageElement.attributeValue("key"), is(equalTo("my-issue-tab-page")));
        assertThat(issueTabPageElement.element("order").getText(), is(equalTo("99")));
        assertThat(issueTabPageElement.attributeValue("url"), is(equalTo("http://www.google.com")));
        Element label = issueTabPageElement.element("label");
        assertThat(label.attributeValue("key"), is(equalTo("my.issuetabpage")));
        assertThat(label.getText(), is(equalTo("My Issue Tab Page")));
        assertThat(issueTabPageElement.attributeValue("name"), is(equalTo("My Issue Tab Page")));
    }
}
