package com.atlassian.plugin.connect.plugin.module.jira.workflow;

import com.atlassian.webhooks.api.register.listener.WebHookListener;
import com.atlassian.webhooks.spi.EventMatcher;
import com.atlassian.webhooks.spi.EventSerializer;
import org.json.JSONObject;

public class RemoteWorkflowPostFunctionEvent
{
    public static final String REMOTE_WORKFLOW_POST_FUNCTION_EVENT_ID = "remote_workflow_post_function";

    private final String fullModuleKey;
    private final JSONObject jsonObject;

    public RemoteWorkflowPostFunctionEvent(final String fullModuleKey, final JSONObject jsonObject)
    {
        this.fullModuleKey = fullModuleKey;
        this.jsonObject = jsonObject;
    }

    public String getJson()
    {
        return jsonObject.toString();
    }

    public static final class FunctionEventMatcher implements EventMatcher<RemoteWorkflowPostFunctionEvent>
    {
        @Override
        public boolean matches(final RemoteWorkflowPostFunctionEvent event, final WebHookListener listener)
        {
            return event.fullModuleKey.equals(listener.getParameters().getFilter());
        }
    }

    public static final class FunctionEventSerializerFactory implements EventSerializer<RemoteWorkflowPostFunctionEvent>
    {
        @Override
        public String serialize(final RemoteWorkflowPostFunctionEvent event)
        {
            return event.getJson();
        }
    }
}