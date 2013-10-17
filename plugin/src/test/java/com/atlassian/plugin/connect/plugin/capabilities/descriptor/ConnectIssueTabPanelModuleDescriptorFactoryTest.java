package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectIssueTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.module.ConditionProcessor;
import com.atlassian.plugin.connect.spi.module.DynamicMarkerCondition;
import com.atlassian.plugin.module.ContainerAccessor;
import com.atlassian.plugin.module.ContainerManagedPlugin;
import com.atlassian.plugin.web.WebFragmentHelper;
import com.atlassian.plugin.web.WebInterfaceManager;
import org.dom4j.Element;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.osgi.framework.BundleContext;

import javax.servlet.http.HttpServletRequest;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectIssueTabPanelCapabilityBean.newIssueTabPageBean;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
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
    private WebInterfaceManager webInterfaceManager;
    @Mock
    private WebFragmentHelper webFragmentHelper;
    @Mock
    private HttpServletRequest servletRequest;

    @Mock
    private IconModuleFragmentFactory iconModuleFragmentFactory;
    @Mock
    private ConditionProcessor conditionProcessor;


    @Mock
    private ContainerAccessor containerAccessor;

    @Mock
    private ConnectIssueTabPanelModuleDescriptor connectIssueTabPanelModuleDescriptor;

    @Before
    public void setup()
    {
        when(plugin.getKey()).thenReturn("my-key");
        when(plugin.getName()).thenReturn("My Plugin");

        when(plugin.getContainerAccessor()).thenReturn(containerAccessor);
        when(containerAccessor.createBean(ConnectIssueTabPanelModuleDescriptor.class)).thenReturn(connectIssueTabPanelModuleDescriptor);

        issueTabPageFactory = new ConnectIssueTabPanelModuleDescriptorFactory(iconModuleFragmentFactory);

        when(servletRequest.getContextPath()).thenReturn("http://ondemand.com/jira");

        when(webInterfaceManager.getWebFragmentHelper()).thenReturn(webFragmentHelper);

        when(webFragmentHelper.renderVelocityFragment(anyString(), anyMap())).thenAnswer(
                new Answer<Object>()
                {
                    @Override
                    public Object answer(InvocationOnMock invocationOnMock) throws Throwable
                    {
                        Object[] args = invocationOnMock.getArguments();
                        return (String) args[0];
                    }
                }
        );

        when(conditionProcessor.getLoadablePlugin(plugin)).thenReturn(plugin);


    }

    @Test
    public void simpleDescriptorCreation() throws Exception
    {

        when(webFragmentHelper.loadCondition(anyString(), any(Plugin.class))).thenReturn(new DynamicMarkerCondition());

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
        assertThat(issueTabPageElement.element("order").getText(), is(equalTo("100")));
        assertThat(issueTabPageElement.attributeValue("url"), is(equalTo("http://www.google.com")));
        Element label = issueTabPageElement.element("label");
        assertThat(label.attributeValue("key"), is(equalTo("my.issuetabpage")));
        assertThat(label.getText(), is(equalTo("My Issue Tab Page")));
        assertThat(issueTabPageElement.attributeValue("name"), is(equalTo("My Issue Tab Page")));
    }
}
