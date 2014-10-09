package com.atlassian.plugin.connect.plugin.product.jira;

import com.atlassian.plugin.connect.plugin.module.jira.workflow.RemoteWorkflowPostFunctionEvent;
import com.atlassian.webhooks.api.register.WebHookPluginRegistration;
import com.atlassian.webhooks.spi.WebHookPluginRegistrationFactory;

import static com.atlassian.webhooks.api.register.RegisteredWebHookEvent.withId;

public final class JiraWebHookPluginRegistrationFactory implements WebHookPluginRegistrationFactory
{
    @Override
    public WebHookPluginRegistration createPluginRegistration()
    {
        return WebHookPluginRegistration.builder()
                .addWebHook(withId(RemoteWorkflowPostFunctionEvent.REMOTE_WORKFLOW_POST_FUNCTION_EVENT_ID)
                        .firedWhen(RemoteWorkflowPostFunctionEvent.class)
                        .isMatchedBy(new RemoteWorkflowPostFunctionEvent.FunctionEventMatcher()))
                .eventSerializer(RemoteWorkflowPostFunctionEvent.class, new RemoteWorkflowPostFunctionEvent.FunctionEventSerializerFactory())
                .build();
    }
}
