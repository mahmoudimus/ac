package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.jira.plugin.ComponentClassManager;
import com.atlassian.jira.plugin.workflow.WorkflowFunctionModuleDescriptor;
import com.atlassian.jira.workflow.OSWorkflowConfigurator;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WorkflowPostFunctionCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.UrlBean;
import com.atlassian.plugin.connect.plugin.capabilities.testobjects.ConnectAutowireUtilForTests;
import com.atlassian.plugin.connect.plugin.capabilities.testobjects.PluginForTests;
import com.atlassian.plugin.connect.plugin.capabilities.util.DelegatingComponentAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WorkflowPostFunctionModuleDescriptorFactoryTest
{

    private Plugin plugin;
    private ConnectAutowireUtilForTests connectAutowireUtil;
    private WorkflowPostFunctionModuleDescriptorFactory wfPostFunctionFactory;

    @Mock
    private DelegatingComponentAccessor componentAccessor;
    @Mock
    private OSWorkflowConfigurator osWorkflowConfigurator;
    @Mock
    private ComponentClassManager componentClassManager;

    @Before
    public void setup()
    {
        plugin = new PluginForTests("my-key", "My Plugin");

        connectAutowireUtil = new ConnectAutowireUtilForTests();
        connectAutowireUtil.defineMock(DelegatingComponentAccessor.class, componentAccessor);

        when(componentAccessor.getComponent(OSWorkflowConfigurator.class)).thenReturn(osWorkflowConfigurator);
        when(componentAccessor.getComponent(ComponentClassManager.class)).thenReturn(componentClassManager);

        wfPostFunctionFactory = new WorkflowPostFunctionModuleDescriptorFactory(connectAutowireUtil);
    }

    @Test
    public void simpleDescriptorCreation() throws Exception
    {
        WorkflowPostFunctionCapabilityBean bean = WorkflowPostFunctionCapabilityBean.newWorkflowPostFunctionBean()
                .withName(new I18nProperty("My Post Function", "my.pf.name"))
                .withDescription(new I18nProperty("Some description", "my.pf.desc"))
                .withCreate(new UrlBean("/create"))
                .withView(new UrlBean("/view"))
                .withTriggered(new UrlBean("/triggered"))
                .build();

        WorkflowFunctionModuleDescriptor descriptor = wfPostFunctionFactory.createModuleDescriptor(plugin, mock(BundleContext.class), bean);

        assertEquals("my-key:my-post-function", descriptor.getCompleteKey());
        assertEquals("My Post Function", descriptor.getName());
        assertEquals("Some description", descriptor.getDescription());
        assertFalse(descriptor.isEditable());
    }
}
