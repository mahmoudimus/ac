package com.atlassian.plugin.connect.plugin.module.jira.workflow;

import com.atlassian.webhooks.spi.provider.EventMatcher;
import com.atlassian.webhooks.spi.provider.EventSerializationException;
import com.atlassian.webhooks.spi.provider.EventSerializer;
import com.atlassian.webhooks.spi.provider.EventSerializerFactory;
import com.atlassian.webhooks.spi.provider.PluginModuleListenerParameters;
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

    public boolean matches(final PluginModuleListenerParameters consumerParams)
    {
        String fullModuleKey = consumerParams.getPluginKey() + consumerParams.getModuleKey().get();
        return this.fullModuleKey.equals(fullModuleKey);
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