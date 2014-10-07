package com.atlassian.plugin.connect.plugin.product.jira;

import com.atlassian.plugin.connect.plugin.module.jira.workflow.RemoteWorkflowPostFunctionEvent;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.webhooks.api.register.*;
import com.atlassian.webhooks.spi.WebHookPluginRegistrationFactory;

import static com.atlassian.webhooks.api.register.RegisteredWebHookEvent.withId;

@JiraComponent
public final class JiraWebHookPluginRegistrationFactory implements WebHookPluginRegistrationFactory
{
    @Override
    public WebHookPluginRegistration createPluginRegistration()
    {
        return WebHookPluginRegistration.builder()
                .addWebHookSection(
                        WebHookEventSection.section("jira-connect-section")
                                .addGroup(
                                        WebHookEventGroup.builder()
                                                .addEvent(
                                                        withId(RemoteWorkflowPostFunctionEvent.REMOTE_WORKFLOW_POST_FUNCTION_EVENT_ID)
                                                                .firedWhen(RemoteWorkflowPostFunctionEvent.class)
                                                                .isMatchedBy(new RemoteWorkflowPostFunctionEvent.FunctionEventMatcher())
                                                )
                                                .build()
                                )
                                .build()
                )
                .eventSerializer(RemoteWorkflowPostFunctionEvent.class, new RemoteWorkflowPostFunctionEvent.FunctionEventSerializerFactory())
                .build();
    }
}
