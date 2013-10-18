package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.ComponentClassManager;
import com.atlassian.jira.plugin.workflow.WorkflowFunctionModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.workflow.OSWorkflowConfigurator;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WorkflowPostFunctionCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.UrlBean;
import com.atlassian.plugin.connect.plugin.capabilities.testobjects.PluginForTests;
import com.atlassian.plugin.connect.plugin.capabilities.util.DelegatingComponentAccessor;
import com.atlassian.plugin.connect.plugin.product.jira.JiraRestBeanMarshaler;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.web.WebFragmentHelper;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.webresource.WebResourceUrlProvider;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.webhooks.spi.provider.ModuleDescriptorWebHookListenerRegistry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.BundleContext;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since version
 */
public class WorkflowPostFunctionModuleDescriptorFactoryTest
{
    private static class TestComponentAccessor extends DelegatingComponentAccessor {

        private final OSWorkflowConfigurator osWorkflowConfigurator = mock(OSWorkflowConfigurator.class);
        private final ComponentClassManager componentClassManager = mock(ComponentClassManager.class);

        @Override
        public <T> T getComponent(Class<T> componentClass)
        {
            if (OSWorkflowConfigurator.class == componentClass) {
                return (T) osWorkflowConfigurator;
            }
            if (ComponentClassManager.class == componentClass) {
                return (T) componentClassManager;
            }
            throw new IllegalStateException();
        }
    }

    private Plugin plugin;
    private WorkflowPostFunctionModuleDescriptorFactory wfPostFunctionFactory;

    @Before
    public void setup()
    {
        plugin = new PluginForTests("my-key", "My Plugin");

        final JiraAuthenticationContext authenticationContext = mock(JiraAuthenticationContext.class);
        final ModuleFactory moduleFactory = mock(ModuleFactory.class);
        final IFrameRenderer iFrameRenderer = mock(IFrameRenderer.class);
        final JiraRestBeanMarshaler jiraRestBeanMarshaler = mock(JiraRestBeanMarshaler.class);
        final ModuleDescriptorWebHookListenerRegistry webHookConsumerRegistry = mock(ModuleDescriptorWebHookListenerRegistry.class);
        final EventPublisher eventPublisher = mock(EventPublisher.class);
        final TemplateRenderer templateRenderer = mock(TemplateRenderer.class);
        final WebResourceUrlProvider webResourceUrlProvider = mock(WebResourceUrlProvider.class);
        final PluginRetrievalService pluginRetrievalService = mock(PluginRetrievalService.class);

        wfPostFunctionFactory = new WorkflowPostFunctionModuleDescriptorFactory(
                authenticationContext, moduleFactory, iFrameRenderer, jiraRestBeanMarshaler, webHookConsumerRegistry,
                eventPublisher, templateRenderer, webResourceUrlProvider, pluginRetrievalService)
        {
            @Override
            protected DelegatingComponentAccessor createDelegatingComponentAccessor()
            {
                return new TestComponentAccessor();
            }
        };
    }

    @Test
    public void simpleBeanCreation() throws Exception
    {
        WorkflowPostFunctionCapabilityBean bean = WorkflowPostFunctionCapabilityBean.newWorkflowPostFunctionBean()
                .withName(new I18nProperty("My Post Function", "my.pf.name"))
                .withDescription(new I18nProperty("Some description", "my.pf.desc"))
                .withCreate(new UrlBean("/create"))
                .withEdit(new UrlBean("/edit"))
                .withView(new UrlBean("/view"))
                .withTriggered(new UrlBean("http://example.com/endpoint"))
                .build();

        assertEquals("My Post Function", bean.getName().getValue());
        assertEquals("Some description", bean.getDescription().getValue());
        assertEquals("/create", bean.getCreate().getUrl());
        assertFalse(bean.getCreate().isAbsolute());
        assertEquals("/edit", bean.getEdit().getUrl());
        assertFalse(bean.getCreate().isAbsolute());
        assertEquals("/view", bean.getView().getUrl());
        assertFalse(bean.getCreate().isAbsolute());
        assertEquals("http://example.com/endpoint", bean.getTriggered().getUrl());
        assertTrue(bean.getTriggered().isAbsolute());
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
