package com.atlassian.plugin.connect.jira.workflow;

import com.atlassian.plugin.connect.jira.workflow.RemoteWorkflowPostFunctionEvent;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.webhooks.spi.provider.WebHookProvider;
import com.atlassian.webhooks.spi.provider.WebHookRegistrar;

@ExportAsService
@JiraComponent
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
