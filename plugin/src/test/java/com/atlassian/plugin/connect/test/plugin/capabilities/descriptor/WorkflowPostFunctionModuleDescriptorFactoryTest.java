package com.atlassian.plugin.connect.test.plugin.capabilities.descriptor;

import com.atlassian.jira.plugin.ComponentClassManager;
import com.atlassian.jira.plugin.workflow.WorkflowFunctionModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.workflow.OSWorkflowConfigurator;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.WorkflowPostFunctionModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.UrlBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.workflow.WorkflowPostFunctionModuleDescriptorFactory;
import com.atlassian.plugin.connect.test.plugin.capabilities.testobjects.ConnectContainerUtilForTests;
import com.atlassian.plugin.connect.plugin.capabilities.util.DelegatingComponentAccessor;
import com.atlassian.plugin.connect.plugin.module.jira.workflow.RemoteWorkflowFunctionPluginFactory;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import com.atlassian.plugin.elements.ResourceDescriptor;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceUrlProvider;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static com.atlassian.jira.plugin.workflow.JiraWorkflowPluginConstants.*;
import static com.atlassian.plugin.connect.test.plugin.capabilities.beans.matchers.IFrameContextMatchers.hasIFramePath;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WorkflowPostFunctionModuleDescriptorFactoryTest
{
    private ConnectContainerUtilForTests connectAutowireUtil;
    private WorkflowPostFunctionModuleDescriptorFactory wfPostFunctionFactory;

    @Mock
    private Plugin plugin;
    @Mock
    private DelegatingComponentAccessor componentAccessor;
    @Mock
    private OSWorkflowConfigurator osWorkflowConfigurator;
    @Mock
    private ComponentClassManager componentClassManager;
    @Mock
    private PluginRetrievalService pluginRetrievalService;
    @Mock
    private ModuleDescriptor moduleDescriptor;
    @Mock
    private WebResourceUrlProvider webResourceUrlProvider;
    @Mock
    private ResourceDescriptor resourceDescriptor;
    @Mock
    private IFrameRenderer iFrameRenderer;
    @Mock
    private JiraAuthenticationContext jiraAuthenticationContext;
    @Mock
    private ApplicationUser applicationUser;

    @Before
    public void setup() throws IOException
    {
        when(plugin.getName()).thenReturn("My Plugin");
        when(plugin.getKey()).thenReturn("my-key");

        connectAutowireUtil = new ConnectContainerUtilForTests()
                .defineMock(DelegatingComponentAccessor.class, componentAccessor)
                .defineMock(PluginRetrievalService.class, pluginRetrievalService)
                .defineMock(WebResourceUrlProvider.class, webResourceUrlProvider)
                .defineMock(JiraAuthenticationContext.class, jiraAuthenticationContext)
                .defineMock(IFrameRenderer.class, iFrameRenderer);

        when(jiraAuthenticationContext.getUser()).thenReturn(applicationUser);
        when(applicationUser.getDisplayName()).thenReturn("tester");

        when(componentAccessor.getComponent(OSWorkflowConfigurator.class)).thenReturn(osWorkflowConfigurator);
        when(componentAccessor.getComponent(ComponentClassManager.class)).thenReturn(componentClassManager);

        when(pluginRetrievalService.getPlugin()).thenReturn(plugin);
        when(plugin.getModuleDescriptor("dialog")).thenReturn(moduleDescriptor);

        when(moduleDescriptor.getResourceDescriptors()).thenReturn(Collections.<ResourceDescriptor>singletonList(resourceDescriptor));
        when(webResourceUrlProvider.getStaticPluginResourceUrl(any(ModuleDescriptor.class), anyString(), any(UrlMode.class))).thenReturn("test.js");

        wfPostFunctionFactory = new WorkflowPostFunctionModuleDescriptorFactory(connectAutowireUtil);
    }

    @Test
    public void verifyDescriptorKeyIsSet() throws Exception
    {
        WorkflowPostFunctionModuleBean bean = WorkflowPostFunctionModuleBean.newWorkflowPostFunctionBean()
                .withName(new I18nProperty("My Post Function", null))
                .withKey("my-post-function")
                .withTriggered(new UrlBean("/callme"))
                .build();

        WorkflowFunctionModuleDescriptor descriptor = wfPostFunctionFactory.createModuleDescriptor(plugin, bean);

        assertEquals("my-key:my-post-function", descriptor.getCompleteKey());
    }

    @Test
    public void verifyNameIsSet() throws Exception
    {
        WorkflowPostFunctionModuleBean bean = WorkflowPostFunctionModuleBean.newWorkflowPostFunctionBean()
                .withName(new I18nProperty("My Post Function", null))
                .withTriggered(new UrlBean("/callme"))
                .build();

        WorkflowFunctionModuleDescriptor descriptor = wfPostFunctionFactory.createModuleDescriptor(plugin, bean);

        assertEquals("My Post Function", descriptor.getName());
    }

    @Test
    public void verifyDescriptionIsSet() throws Exception
    {
        WorkflowPostFunctionModuleBean bean = WorkflowPostFunctionModuleBean.newWorkflowPostFunctionBean()
                .withDescription(new I18nProperty("Some description", null))
                .withTriggered(new UrlBean("/callme"))
                .build();

        WorkflowFunctionModuleDescriptor descriptor = wfPostFunctionFactory.createModuleDescriptor(plugin, bean);

        assertEquals("Some description", descriptor.getDescription());
    }

    @Test
    public void verifyIsEditable() throws Exception
    {
        WorkflowPostFunctionModuleBean bean = WorkflowPostFunctionModuleBean.newWorkflowPostFunctionBean()
                .withCreate(new UrlBean("/create"))
                .withEdit(new UrlBean("/edit"))
                .withTriggered(new UrlBean("/callme"))
                .build();

        WorkflowFunctionModuleDescriptor descriptor = wfPostFunctionFactory.createModuleDescriptor(plugin, bean);

        assertTrue(descriptor.isEditable());
    }

    @Test
    public void verifyIsNotEditable() throws Exception
    {
        WorkflowPostFunctionModuleBean bean = WorkflowPostFunctionModuleBean.newWorkflowPostFunctionBean()
                .withView(new UrlBean("/view"))
                .withTriggered(new UrlBean("/callme"))
                .build();

        WorkflowFunctionModuleDescriptor descriptor = wfPostFunctionFactory.createModuleDescriptor(plugin, bean);

        assertFalse(descriptor.isEditable());
    }

    @Test
    public void verifyResourceDescriptorsArePresent() throws Exception
    {
        WorkflowPostFunctionModuleBean bean = WorkflowPostFunctionModuleBean.newWorkflowPostFunctionBean()
                .withView(new UrlBean("/view"))
                .withEdit(new UrlBean(("/edit")))
                .withCreate(new UrlBean("/create"))
                .withTriggered(new UrlBean("/callme"))
                .build();

        WorkflowFunctionModuleDescriptor descriptor = wfPostFunctionFactory.createModuleDescriptor(plugin, bean);

        assertEquals(3, descriptor.getResourceDescriptors(RESOURCE_TYPE_VELOCITY).size());
    }

    @Test
    public void verifyIsDeletable() throws Exception
    {
        WorkflowPostFunctionModuleBean bean = WorkflowPostFunctionModuleBean.newWorkflowPostFunctionBean()
                .withTriggered(new UrlBean("/callme"))
                .build();

        WorkflowFunctionModuleDescriptor descriptor = wfPostFunctionFactory.createModuleDescriptor(plugin, bean);

        assertTrue(descriptor.isDeletable());
    }

    @Test
    public void verifyIsOrderable() throws Exception
    {
        WorkflowPostFunctionModuleBean bean = WorkflowPostFunctionModuleBean.newWorkflowPostFunctionBean()
                .withTriggered(new UrlBean("/callme"))
                .build();

        WorkflowFunctionModuleDescriptor descriptor = wfPostFunctionFactory.createModuleDescriptor(plugin, bean);

        assertTrue(descriptor.isOrderable());
    }

    @Test
    public void verifyIsNotUnique() throws Exception
    {
        WorkflowPostFunctionModuleBean bean = WorkflowPostFunctionModuleBean.newWorkflowPostFunctionBean()
                .withTriggered(new UrlBean("/callme"))
                .build();

        WorkflowFunctionModuleDescriptor descriptor = wfPostFunctionFactory.createModuleDescriptor(plugin, bean);

        assertFalse(descriptor.isUnique());
    }

    @Test
    public void verifyIsNoSystemModule() throws Exception
    {
        WorkflowPostFunctionModuleBean bean = WorkflowPostFunctionModuleBean.newWorkflowPostFunctionBean()
                .withTriggered(new UrlBean("/callme"))
                .build();

        WorkflowFunctionModuleDescriptor descriptor = wfPostFunctionFactory.createModuleDescriptor(plugin, bean);

        assertFalse(descriptor.isSystemModule());
    }

    @Test
    public void verifyIsEnabledByDefault() throws Exception
    {
        WorkflowPostFunctionModuleBean bean = WorkflowPostFunctionModuleBean.newWorkflowPostFunctionBean()
                .withTriggered(new UrlBean("/callme"))
                .build();

        WorkflowFunctionModuleDescriptor descriptor = wfPostFunctionFactory.createModuleDescriptor(plugin, bean);

        assertTrue(descriptor.isEnabledByDefault());
    }

    @Test
    public void verifyCreateUrl() throws Exception
    {
        WorkflowPostFunctionModuleBean bean = WorkflowPostFunctionModuleBean.newWorkflowPostFunctionBean()
                .withTriggered(new UrlBean("/callme"))
                .withCreate(new UrlBean("/create"))
                .build();

        WorkflowFunctionModuleDescriptor descriptor = wfPostFunctionFactory.createModuleDescriptor(plugin, bean);

        ResourceDescriptor resource = descriptor.getResourceDescriptor(RESOURCE_TYPE_VELOCITY, RESOURCE_NAME_INPUT_PARAMETERS);
        assertEquals("/create", resource.getLocation());
    }

    @Test
    public void verifyEditUrl() throws Exception
    {
        WorkflowPostFunctionModuleBean bean = WorkflowPostFunctionModuleBean.newWorkflowPostFunctionBean()
                .withTriggered(new UrlBean("/callme"))
                .withEdit(new UrlBean("/edit"))
                .build();

        WorkflowFunctionModuleDescriptor descriptor = wfPostFunctionFactory.createModuleDescriptor(plugin, bean);

        ResourceDescriptor resource = descriptor.getResourceDescriptor(RESOURCE_TYPE_VELOCITY, RESOURCE_NAME_EDIT_PARAMETERS);
        assertEquals("/edit", resource.getLocation());
    }

    @Test
    public void verifyViewUrl() throws Exception
    {
        WorkflowPostFunctionModuleBean bean = WorkflowPostFunctionModuleBean.newWorkflowPostFunctionBean()
                .withTriggered(new UrlBean("/callme"))
                .withView(new UrlBean("/view"))
                .build();

        WorkflowFunctionModuleDescriptor descriptor = wfPostFunctionFactory.createModuleDescriptor(plugin, bean);

        ResourceDescriptor resource = descriptor.getResourceDescriptor(RESOURCE_TYPE_VELOCITY, RESOURCE_NAME_VIEW);
        assertEquals("/view", resource.getLocation());
    }

    @Test
    @Ignore("TODO tim to fix")
    public void verifyIFrameURL() throws Exception
    {
        WorkflowPostFunctionModuleBean bean = WorkflowPostFunctionModuleBean.newWorkflowPostFunctionBean()
                .withTriggered(new UrlBean("/callme"))
                .withView(new UrlBean("/view"))
                .build();

        UUID uuid = UUID.randomUUID();
        Map<String, String> startingParams = Collections.singletonMap(RemoteWorkflowFunctionPluginFactory.STORED_POSTFUNCTION_ID, uuid.toString());

        WorkflowFunctionModuleDescriptor descriptor = wfPostFunctionFactory.createModuleDescriptor(plugin, bean);

        descriptor.getHtml(RESOURCE_NAME_VIEW, startingParams);
        verify(iFrameRenderer).render(argThat(hasIFramePath("/view")), anyString(), anyMap(), anyString(), anyMap());
    }
}
