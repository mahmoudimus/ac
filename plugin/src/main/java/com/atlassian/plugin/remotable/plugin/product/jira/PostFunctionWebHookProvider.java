package com.atlassian.plugin.remotable.plugin.product.jira;

import com.atlassian.plugin.remotable.plugin.module.jira.workflow.RemoteWorkflowPostFunctionEvent;
import com.atlassian.webhooks.spi.provider.WebHookProvider;
import com.atlassian.webhooks.spi.provider.WebHookRegistrar;

public class PostFunctionWebHookProvider implements WebHookProvider
{

    @Override
    public void provide(WebHookRegistrar publish)
    {
        publish.webhook(RemoteWorkflowPostFunctionEvent.REMOTE_WORKFLOW_POST_FUNCTION_EVENT_ID)
                .whenFired(RemoteWorkflowPostFunctionEvent.class)
                .matchedBy(new RemoteWorkflowPostFunctionEvent.FunctionEventMatcher())
                .serializedWith(new RemoteWorkflowPostFunctionEvent.FunctionEventSerializerFactory());
    }

}
