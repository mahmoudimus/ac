package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.jira.plugin.ComponentClassManager;
import com.atlassian.jira.plugin.workflow.WorkflowFunctionModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static com.atlassian.jira.plugin.workflow.JiraWorkflowPluginConstants.RESOURCE_TYPE_VELOCITY;

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
    public void verifyDescriptorKeyIsSet() throws Exception
    {
        WorkflowPostFunctionCapabilityBean bean = WorkflowPostFunctionCapabilityBean.newWorkflowPostFunctionBean()
                .withName(new I18nProperty("My Post Function", null))
                .withTriggered(new UrlBean("/callme"))
                .build();

        WorkflowFunctionModuleDescriptor descriptor = wfPostFunctionFactory.createModuleDescriptor(plugin, mock(BundleContext.class), bean);

        assertEquals("my-key:my-post-function", descriptor.getCompleteKey());
    }

    @Test
    public void verifyNameIsSet() throws Exception
    {
        WorkflowPostFunctionCapabilityBean bean = WorkflowPostFunctionCapabilityBean.newWorkflowPostFunctionBean()
                .withName(new I18nProperty("My Post Function", null))
                .withTriggered(new UrlBean("/callme"))
                .build();

        WorkflowFunctionModuleDescriptor descriptor = wfPostFunctionFactory.createModuleDescriptor(plugin, mock(BundleContext.class), bean);

        assertEquals("My Post Function", descriptor.getName());
    }

    @Test
    public void verifyDescriptionIsSet() throws Exception
    {
        WorkflowPostFunctionCapabilityBean bean = WorkflowPostFunctionCapabilityBean.newWorkflowPostFunctionBean()
                .withDescription(new I18nProperty("Some description", null))
                .withTriggered(new UrlBean("/callme"))
                .build();

        WorkflowFunctionModuleDescriptor descriptor = wfPostFunctionFactory.createModuleDescriptor(plugin, mock(BundleContext.class), bean);

        assertEquals("Some description", descriptor.getDescription());
    }

    @Test
    public void verifyIsEditable() throws Exception
    {
        WorkflowPostFunctionCapabilityBean bean = WorkflowPostFunctionCapabilityBean.newWorkflowPostFunctionBean()
                .withCreate(new UrlBean("/create"))
                .withEdit(new UrlBean("/edit"))
                .withTriggered(new UrlBean("/callme"))
                .build();

        WorkflowFunctionModuleDescriptor descriptor = wfPostFunctionFactory.createModuleDescriptor(plugin, mock(BundleContext.class), bean);

        assertTrue(descriptor.isEditable());
    }

    @Test
    public void verifyIsNotEditable() throws Exception
    {
        WorkflowPostFunctionCapabilityBean bean = WorkflowPostFunctionCapabilityBean.newWorkflowPostFunctionBean()
                .withView(new UrlBean("/view"))
                .withTriggered(new UrlBean("/callme"))
                .build();

        WorkflowFunctionModuleDescriptor descriptor = wfPostFunctionFactory.createModuleDescriptor(plugin, mock(BundleContext.class), bean);

        assertFalse(descriptor.isEditable());
    }

    @Test
    public void verifyResourceDescriptorsArePresent() throws Exception
    {
        WorkflowPostFunctionCapabilityBean bean = WorkflowPostFunctionCapabilityBean.newWorkflowPostFunctionBean()
                .withView(new UrlBean("/view"))
                .withEdit(new UrlBean(("/edit")))
                .withCreate(new UrlBean("/create"))
                .withTriggered(new UrlBean("/callme"))
                .build();

        WorkflowFunctionModuleDescriptor descriptor = wfPostFunctionFactory.createModuleDescriptor(plugin, mock(BundleContext.class), bean);

        assertEquals(3, descriptor.getResourceDescriptors(RESOURCE_TYPE_VELOCITY).size());
    }

    @Test
    public void verifyIsDeletable() throws Exception
    {
        WorkflowPostFunctionCapabilityBean bean = WorkflowPostFunctionCapabilityBean.newWorkflowPostFunctionBean()
                .withTriggered(new UrlBean("/callme"))
                .build();

        WorkflowFunctionModuleDescriptor descriptor = wfPostFunctionFactory.createModuleDescriptor(plugin, mock(BundleContext.class), bean);

        assertTrue(descriptor.isDeletable());
    }

    @Test
    public void verifyIsOrderable() throws Exception
    {
        WorkflowPostFunctionCapabilityBean bean = WorkflowPostFunctionCapabilityBean.newWorkflowPostFunctionBean()
                .withTriggered(new UrlBean("/callme"))
                .build();

        WorkflowFunctionModuleDescriptor descriptor = wfPostFunctionFactory.createModuleDescriptor(plugin, mock(BundleContext.class), bean);

        assertTrue(descriptor.isOrderable());
    }

    @Test
    public void verifyIsNotUnique() throws Exception
    {
        WorkflowPostFunctionCapabilityBean bean = WorkflowPostFunctionCapabilityBean.newWorkflowPostFunctionBean()
                .withTriggered(new UrlBean("/callme"))
                .build();

        WorkflowFunctionModuleDescriptor descriptor = wfPostFunctionFactory.createModuleDescriptor(plugin, mock(BundleContext.class), bean);

        assertFalse(descriptor.isUnique());
    }

    @Test
    public void verifyIsNoSystemModule() throws Exception
    {
        WorkflowPostFunctionCapabilityBean bean = WorkflowPostFunctionCapabilityBean.newWorkflowPostFunctionBean()
                .withTriggered(new UrlBean("/callme"))
                .build();

        WorkflowFunctionModuleDescriptor descriptor = wfPostFunctionFactory.createModuleDescriptor(plugin, mock(BundleContext.class), bean);

        assertFalse(descriptor.isSystemModule());
    }

    @Test
    public void verifyIsEnabledByDefault() throws Exception
    {
        WorkflowPostFunctionCapabilityBean bean = WorkflowPostFunctionCapabilityBean.newWorkflowPostFunctionBean()
                .withTriggered(new UrlBean("/callme"))
                .build();

        WorkflowFunctionModuleDescriptor descriptor = wfPostFunctionFactory.createModuleDescriptor(plugin, mock(BundleContext.class), bean);

        assertTrue(descriptor.isEnabledByDefault());
    }
}
