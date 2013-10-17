package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectVersionTabPanelCapabilityBean;
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

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectVersionTabPanelCapabilityBean.newVersionTabPageBean;
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
public class ConnectVersionTabPanelModuleDescriptorFactoryTest
{
    private ConnectVersionTabPanelModuleDescriptorFactory versionTabPageFactory;

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
    private ConnectVersionTabPanelModuleDescriptor connectVersionTabPanelModuleDescriptor;

    @Before
    public void setup()
    {
        when(plugin.getKey()).thenReturn("my-key");
        when(plugin.getName()).thenReturn("My Plugin");

        when(plugin.getContainerAccessor()).thenReturn(containerAccessor);
        when(containerAccessor.createBean(ConnectVersionTabPanelModuleDescriptor.class)).thenReturn(connectVersionTabPanelModuleDescriptor);

        versionTabPageFactory = new ConnectVersionTabPanelModuleDescriptorFactory();

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
        assertThat(versionTabPageElement.element("order").getText(), is(equalTo("100")));
        assertThat(versionTabPageElement.attributeValue("url"), is(equalTo("http://www.google.com")));
        Element label = versionTabPageElement.element("label");
        assertThat(label.attributeValue("key"), is(equalTo("my.versiontabpage")));
        assertThat(label.getText(), is(equalTo("My Version Tab Page")));
        assertThat(versionTabPageElement.attributeValue("name"), is(equalTo("My Version Tab Page")));
    }
}
