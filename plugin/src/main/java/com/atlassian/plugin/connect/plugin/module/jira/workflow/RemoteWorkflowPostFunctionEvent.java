package com.atlassian.plugin.connect.plugin.module.jira.workflow;

import com.atlassian.webhooks.spi.provider.*;

import org.json.JSONObject;

public class RemoteWorkflowPostFunctionEvent
{
    public static final String REMOTE_WORKFLOW_POST_FUNCTION_EVENT_ID = "remote_workflow_post_function";

    private final String pluginKey;
    private final String moduleKey;
    private final JSONObject jsonObject;

    public RemoteWorkflowPostFunctionEvent(final String pluginKey, final String moduleKey, final JSONObject jsonObject)
    {
        this.pluginKey = pluginKey;
        this.moduleKey = moduleKey;
        this.jsonObject = jsonObject;
    }

    public String getJson()
    {
        return jsonObject.toString();
    }

    public boolean matches(final PluginModuleListenerParameters consumerParams)
    {
        return this.pluginKey.equals(consumerParams.getPluginKey())
                && this.moduleKey.equals(consumerParams.getModuleKey().get());
    }

    public static final class FunctionEventMatcher implements EventMatcher<RemoteWorkflowPostFunctionEvent>
    {
        @Override
        public boolean matches(final RemoteWorkflowPostFunctionEvent event, final Object consumerParams)
        {
            return consumerParams instanceof PluginModuleListenerParameters
                    && event.matches((PluginModuleListenerParameters) consumerParams);
        }
    }

    public static final class FunctionEventSerializerFactory implements EventSerializerFactory<RemoteWorkflowPostFunctionEvent>
    {
        @Override
        public EventSerializer create(final RemoteWorkflowPostFunctionEvent event)
        {
            return new EventSerializer()
            {
                @Override
                public Object getEvent()
                {
                    return event;
                }

                @Override
                public String getWebHookBody() throws EventSerializationException
                {
                    return event.getJson();
                }
            };
        }
    }
}