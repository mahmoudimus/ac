package com.atlassian.plugin.connect.jira.workflow;

import com.atlassian.jira.plugin.ComponentClassManager;
import com.atlassian.jira.plugin.workflow.WorkflowFunctionModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.workflow.OSWorkflowConfigurator;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderer;
import com.atlassian.plugin.connect.jira.DelegatingComponentAccessor;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WorkflowPostFunctionModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.UrlBean;
import com.atlassian.plugin.connect.test.annotation.ConvertToWiredTest;
import com.atlassian.plugin.connect.test.fixture.ConnectContainerUtilForTests;
import com.atlassian.plugin.elements.ResourceDescriptor;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceUrlProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Collections;

import static com.atlassian.jira.plugin.workflow.JiraWorkflowPluginConstants.RESOURCE_NAME_EDIT_PARAMETERS;
import static com.atlassian.jira.plugin.workflow.JiraWorkflowPluginConstants.RESOURCE_NAME_INPUT_PARAMETERS;
import static com.atlassian.jira.plugin.workflow.JiraWorkflowPluginConstants.RESOURCE_NAME_VIEW;
import static com.atlassian.jira.plugin.workflow.JiraWorkflowPluginConstants.RESOURCE_TYPE_VELOCITY;
import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@ConvertToWiredTest
@RunWith(MockitoJUnitRunner.class)
public class WorkflowPostFunctionModuleDescriptorFactoryTest {
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
    private WebResourceUrlProvider webResourceUrlProvider;
    @Mock
    private ResourceDescriptor resourceDescriptor;
    @Mock
    private IFrameRenderer iFrameRenderer;
    @Mock
    private JiraAuthenticationContext jiraAuthenticationContext;
    @Mock
    private ApplicationUser applicationUser;

    private ConnectAddonBean addon;

    @Before
    public void setup() throws IOException {
        this.addon = newConnectAddonBean().withKey("my-key").build();

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

        when(webResourceUrlProvider.getStaticPluginResourceUrl(any(ModuleDescriptor.class), anyString(), any(UrlMode.class))).thenReturn("test.js");

        wfPostFunctionFactory = new WorkflowPostFunctionModuleDescriptorFactory(connectAutowireUtil);
    }

    @Test
    public void verifyDescriptorKeyIsSet() throws Exception {
        WorkflowPostFunctionModuleBean bean = WorkflowPostFunctionModuleBean.newWorkflowPostFunctionBean()
                .withName(new I18nProperty("My Post Function", null))
                .withKey("my-post-function")
                .withTriggered(new UrlBean("/callme"))
                .build();

        WorkflowFunctionModuleDescriptor descriptor = wfPostFunctionFactory.createModuleDescriptor(bean, addon, plugin);

        assertEquals("my-key:" + addonAndModuleKey("my-key", "my-post-function"), descriptor.getCompleteKey());
    }

    @Test
    public void verifyNameIsSet() throws Exception {
        WorkflowPostFunctionModuleBean bean = WorkflowPostFunctionModuleBean.newWorkflowPostFunctionBean()
                .withName(new I18nProperty("My Post Function", null))
                .withKey("my-post-function")
                .withTriggered(new UrlBean("/callme"))
                .build();

        WorkflowFunctionModuleDescriptor descriptor = wfPostFunctionFactory.createModuleDescriptor(bean, addon, plugin);

        assertEquals("My Post Function", descriptor.getName());
    }

    @Test
    public void verifyDescriptionIsSet() throws Exception {
        WorkflowPostFunctionModuleBean bean = WorkflowPostFunctionModuleBean.newWorkflowPostFunctionBean()
                .withDescription(new I18nProperty("Some description", null))
                .withKey("my-post-function")
                .withTriggered(new UrlBean("/callme"))
                .build();

        WorkflowFunctionModuleDescriptor descriptor = wfPostFunctionFactory.createModuleDescriptor(bean, addon, plugin);

        assertEquals("Some description", descriptor.getDescription());
    }

    @Test
    public void verifyIsEditable() throws Exception {
        WorkflowPostFunctionModuleBean bean = WorkflowPostFunctionModuleBean.newWorkflowPostFunctionBean()
                .withKey("my-post-function")
                .withCreate(new UrlBean("/create"))
                .withEdit(new UrlBean("/edit"))
                .withTriggered(new UrlBean("/callme"))
                .build();

        WorkflowFunctionModuleDescriptor descriptor = wfPostFunctionFactory.createModuleDescriptor(bean, addon, plugin);

        assertTrue(descriptor.isEditable());
    }

    @Test
    public void verifyIsNotEditable() throws Exception {
        WorkflowPostFunctionModuleBean bean = WorkflowPostFunctionModuleBean.newWorkflowPostFunctionBean()
                .withKey("my-post-function")
                .withView(new UrlBean("/view"))
                .withTriggered(new UrlBean("/callme"))
                .build();

        WorkflowFunctionModuleDescriptor descriptor = wfPostFunctionFactory.createModuleDescriptor(bean, addon, plugin);

        assertFalse(descriptor.isEditable());
    }

    @Test
    public void verifyResourceDescriptorsArePresent() throws Exception {
        WorkflowPostFunctionModuleBean bean = WorkflowPostFunctionModuleBean.newWorkflowPostFunctionBean()
                .withKey("my-post-function")
                .withView(new UrlBean("/view"))
                .withEdit(new UrlBean(("/edit")))
                .withCreate(new UrlBean("/create"))
                .withTriggered(new UrlBean("/callme"))
                .build();

        WorkflowFunctionModuleDescriptor descriptor = wfPostFunctionFactory.createModuleDescriptor(bean, addon, plugin);

        assertEquals(3, descriptor.getResourceDescriptors(RESOURCE_TYPE_VELOCITY).size());
    }

    @Test
    public void verifyIsDeletable() throws Exception {
        WorkflowPostFunctionModuleBean bean = WorkflowPostFunctionModuleBean.newWorkflowPostFunctionBean()
                .withKey("my-post-function")
                .withTriggered(new UrlBean("/callme"))
                .build();

        WorkflowFunctionModuleDescriptor descriptor = wfPostFunctionFactory.createModuleDescriptor(bean, addon, plugin);

        assertTrue(descriptor.isDeletable());
    }

    @Test
    public void verifyIsOrderable() throws Exception {
        WorkflowPostFunctionModuleBean bean = WorkflowPostFunctionModuleBean.newWorkflowPostFunctionBean()
                .withKey("my-post-function")
                .withTriggered(new UrlBean("/callme"))
                .build();

        WorkflowFunctionModuleDescriptor descriptor = wfPostFunctionFactory.createModuleDescriptor(bean, addon, plugin);

        assertTrue(descriptor.isOrderable());
    }

    @Test
    public void verifyIsNotUnique() throws Exception {
        WorkflowPostFunctionModuleBean bean = WorkflowPostFunctionModuleBean.newWorkflowPostFunctionBean()
                .withKey("my-post-function")
                .withTriggered(new UrlBean("/callme"))
                .build();

        WorkflowFunctionModuleDescriptor descriptor = wfPostFunctionFactory.createModuleDescriptor(bean, addon, plugin);

        assertFalse(descriptor.isUnique());
    }

    @Test
    public void verifyIsNoSystemModule() throws Exception {
        WorkflowPostFunctionModuleBean bean = WorkflowPostFunctionModuleBean.newWorkflowPostFunctionBean()
                .withKey("my-post-function")
                .withTriggered(new UrlBean("/callme"))
                .build();

        WorkflowFunctionModuleDescriptor descriptor = wfPostFunctionFactory.createModuleDescriptor(bean, addon, plugin);

        assertFalse(descriptor.isSystemModule());
    }

    @Test
    public void verifyIsEnabledByDefault() throws Exception {
        WorkflowPostFunctionModuleBean bean = WorkflowPostFunctionModuleBean.newWorkflowPostFunctionBean()
                .withKey("my-post-function")
                .withTriggered(new UrlBean("/callme"))
                .build();

        WorkflowFunctionModuleDescriptor descriptor = wfPostFunctionFactory.createModuleDescriptor(bean, addon, plugin);

        assertTrue(descriptor.isEnabledByDefault());
    }

    @Test
    public void verifyCreateUrl() throws Exception {
        WorkflowPostFunctionModuleBean bean = WorkflowPostFunctionModuleBean.newWorkflowPostFunctionBean()
                .withKey("my-post-function")
                .withTriggered(new UrlBean("/callme"))
                .withCreate(new UrlBean("/create"))
                .build();

        WorkflowFunctionModuleDescriptor descriptor = wfPostFunctionFactory.createModuleDescriptor(bean, addon, plugin);

        ResourceDescriptor resource = descriptor.getResourceDescriptor(RESOURCE_TYPE_VELOCITY, RESOURCE_NAME_INPUT_PARAMETERS);
        assertEquals("/create", resource.getLocation());
    }

    @Test
    public void verifyEditUrl() throws Exception {
        WorkflowPostFunctionModuleBean bean = WorkflowPostFunctionModuleBean.newWorkflowPostFunctionBean()
                .withKey("my-post-function")
                .withTriggered(new UrlBean("/callme"))
                .withEdit(new UrlBean("/edit"))
                .build();

        WorkflowFunctionModuleDescriptor descriptor = wfPostFunctionFactory.createModuleDescriptor(bean, addon, plugin);

        ResourceDescriptor resource = descriptor.getResourceDescriptor(RESOURCE_TYPE_VELOCITY, RESOURCE_NAME_EDIT_PARAMETERS);
        assertEquals("/edit", resource.getLocation());
    }

    @Test
    public void verifyViewUrl() throws Exception {
        WorkflowPostFunctionModuleBean bean = WorkflowPostFunctionModuleBean.newWorkflowPostFunctionBean()
                .withKey("my-post-function")
                .withTriggered(new UrlBean("/callme"))
                .withView(new UrlBean("/view"))
                .build();

        WorkflowFunctionModuleDescriptor descriptor = wfPostFunctionFactory.createModuleDescriptor(bean, addon, plugin);

        ResourceDescriptor resource = descriptor.getResourceDescriptor(RESOURCE_TYPE_VELOCITY, RESOURCE_NAME_VIEW);
        assertEquals("/view", resource.getLocation());
    }
}
