package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanelModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.IssueTabPageCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.testobjects.PluginForTests;
import com.atlassian.plugin.connect.plugin.integration.plugins.DescriptorToRegister;
import com.atlassian.plugin.connect.plugin.integration.plugins.DynamicDescriptorRegistration;
import com.atlassian.plugin.connect.plugin.module.ConditionProcessor;
import com.atlassian.plugin.connect.plugin.module.jira.context.serializer.IssueSerializer;
import com.atlassian.plugin.connect.plugin.module.jira.context.serializer.ProjectSerializer;
import com.atlassian.plugin.connect.plugin.module.jira.issuetab.IssueTabPageModuleDescriptor;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlValidator;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.spi.module.DynamicMarkerCondition;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.web.WebFragmentHelper;
import com.atlassian.plugin.web.WebInterfaceManager;
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

import static com.atlassian.plugin.connect.plugin.capabilities.beans.IssueTabPageCapabilityBean.newIssueTabPageBean;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
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
public class IssueTabPageModuleDescriptorFactoryTest
{
    private Plugin plugin;
    private IssueTabPageModuleDescriptorFactory issueTabPageFactory;

    @Mock
    private WebInterfaceManager webInterfaceManager;
    @Mock
    private WebFragmentHelper webFragmentHelper;
    @Mock
    private HttpServletRequest servletRequest;

    @Mock
    private IconModuleFragmentFactory iconModuleFragmentFactory;
    @Mock
    private ModuleFactory moduleFactory;
    @Mock
    private DynamicDescriptorRegistration dynamicDescriptorRegistration;
    @Mock
    private ConditionProcessor conditionProcessor;

    @Mock
    private IFrameRenderer iFrameRenderer;
    @Mock
    private UrlVariableSubstitutor urlVariableSubstitutor;
    @Mock
    private JiraAuthenticationContext jiraAuthenticationContext;
    @Mock
    private UrlValidator urlValidator;
    @Mock
    private ProjectSerializer projectSerializer;
    @Mock
    private IssueSerializer issueSerializer;


    @Before
    public void setup()
    {
        plugin = new PluginForTests("my-key", "My Plugin");

        issueTabPageFactory = new IssueTabPageModuleDescriptorFactory(iconModuleFragmentFactory, moduleFactory, dynamicDescriptorRegistration,
                conditionProcessor, iFrameRenderer, urlVariableSubstitutor,
                jiraAuthenticationContext, urlValidator, projectSerializer, issueSerializer);

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
//        when(dynamicDescriptorRegistration.registerDescriptors(eq(plugin), any(DescriptorToRegister.class))).thenReturn();
    }

    @Test
    public void simpleDescriptorCreation() throws Exception
    {

        when(webFragmentHelper.loadCondition(anyString(), any(Plugin.class))).thenReturn(new DynamicMarkerCondition());

        IssueTabPageCapabilityBean bean = newIssueTabPageBean()
                .withName(new I18nProperty("My Issue Tab Page", "my.issuetabpage"))
                .withUrl("http://www.google.com")
                .withWeight(99)
                .build();

        IssueTabPageModuleDescriptor descriptor = issueTabPageFactory.createModuleDescriptor(plugin, mock(BundleContext.class), bean);
        descriptor.enabled();

        assertEquals("my-key:my-issue-tab-page", descriptor.getCompleteKey());
        assertEquals("My Issue Tab Page", descriptor.getName());
        assertEquals("http://www.google.com", descriptor.getUrl());

        ArgumentCaptor<DescriptorToRegister> argumentCaptor = ArgumentCaptor.forClass(DescriptorToRegister.class);
        verify(dynamicDescriptorRegistration, times(1)).registerDescriptors(eq(plugin), argumentCaptor.capture());
        DescriptorToRegister descriptorToRegister = argumentCaptor.getValue();
        ModuleDescriptor moduleDescriptor = descriptorToRegister.getDescriptor();
        assertThat(moduleDescriptor, is(instanceOf(IssueTabPanelModuleDescriptor.class)));
        IssueTabPanelModuleDescriptor issueTabPanelModuleDescriptor = (IssueTabPanelModuleDescriptor) moduleDescriptor;
        assertThat(issueTabPanelModuleDescriptor.getOrder(), is(equalTo(100)));

//        assertNull(descriptor.getIcon());
    }
}
