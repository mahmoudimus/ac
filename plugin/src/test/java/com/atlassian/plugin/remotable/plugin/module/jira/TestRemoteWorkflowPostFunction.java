package com.atlassian.plugin.remotable.plugin.module.jira;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.workflow.JiraWorkflowPluginConstants;
import com.atlassian.jira.propertyset.JiraCachingPropertySet;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.elements.ResourceDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.remotable.plugin.module.jira.workflow.RemoteWorkflowPostFunctionEvent;
import com.atlassian.plugin.remotable.plugin.module.jira.workflow.RemoteWorkflowPostFunctionModuleDescriptor;
import com.atlassian.plugin.remotable.plugin.module.jira.workflow.RemoteWorkflowPostFunctionProvider;
import com.atlassian.plugin.remotable.plugin.product.jira.JiraRestBeanMarshaler;
import com.atlassian.plugin.remotable.spi.module.IFrameRenderer;
import com.atlassian.plugin.webresource.WebResourceUrlProvider;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.webhooks.spi.provider.ModuleDescriptorWebHookConsumerRegistry;
import com.atlassian.webhooks.spi.provider.PluginModuleConsumerParams;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.opensymphony.workflow.WorkflowException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsCollectionContaining;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.internal.matchers.StringContains.containsString;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class TestRemoteWorkflowPostFunction
{
    @Mock
    private JiraRestBeanMarshaler issueMarshaler;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private TemplateRenderer templateRenderer;

    private final PluginModuleConsumerParams consumerParams = new PluginModuleConsumerParams("plugin", Optional.of("module"), ImmutableMap.<String, Object>of(), RemoteWorkflowPostFunctionEvent.REMOTE_WORKFLOW_POST_FUNCTION_EVENT_ID);

    @Before
    public void setup()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testPostFunctionExecution() throws WorkflowException
    {
        doAnswer(new Answer<Void>()
        {
            @Override
            public Void answer(final InvocationOnMock invocationOnMock) throws Throwable
            {
                final RemoteWorkflowPostFunctionEvent event = (RemoteWorkflowPostFunctionEvent) invocationOnMock.getArguments()[0];
                assertTrue(event.matches(consumerParams));
                assertThat(event.getJson(), containsString("id"));
                assertThat(event.getJson(), containsString("10"));
                assertThat(event.getJson(), containsString("issue_type"));
                assertThat(event.getJson(), containsString("bug"));
                return null;
            }
        }).when(eventPublisher).publish(anyObject());

        RemoteWorkflowPostFunctionProvider postFunctionProvider = new RemoteWorkflowPostFunctionProvider(eventPublisher, issueMarshaler, "plugin", "module")
        {
            @Override
            protected JSONObject postFunctionJSON(final Map<?, ?> transientVars, final Map args)
            {
                return new JSONObject(ImmutableMap.of("id", "10", "issue_type", "bug"));
            }
        };
        postFunctionProvider.execute(ImmutableMap.of(), ImmutableMap.of(), new JiraCachingPropertySet());
    }

    @Test
    public void testResourceValidResourceDescriptors()
    {
        ComponentAccessor.initialiseWorker(mock(ComponentAccessor.Worker.class));
        final Element root = DocumentFactory.getInstance()
                .createDocument()
                .addElement("remote-workflow-post-function");
        root.addAttribute("url", "/some-servlet");
        root.addAttribute("key", "function");
        root.addElement("view")
                .addAttribute("url", "/another-servlet");

        final RemoteWorkflowPostFunctionModuleDescriptor descriptor = new RemoteWorkflowPostFunctionModuleDescriptor(
                mock(JiraAuthenticationContext.class),
                mock(ModuleFactory.class),
                mock(IFrameRenderer.class),
                issueMarshaler,
                mock(ModuleDescriptorWebHookConsumerRegistry.class),
                eventPublisher,
                templateRenderer,
                mock(WebResourceUrlProvider.class),
                mock(PluginRetrievalService.class));

        descriptor.init(Mockito.mock(Plugin.class), root);
        assertEquals(1, descriptor.getResourceDescriptors().size());
        final Matcher<Iterable<? super ResourceDescriptor>> hasItem = IsCollectionContaining.hasItem(new TypeSafeMatcher<ResourceDescriptor>()
        {
            @Override
            public boolean matchesSafely(final ResourceDescriptor resourceDescriptor)
            {
                return resourceDescriptor.getName().equals(JiraWorkflowPluginConstants.RESOURCE_NAME_VIEW);
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("Resource descriptors should contain view descriptor");
            }
        });
        assertThat(descriptor.getResourceDescriptors(), hasItem);
    }



}
