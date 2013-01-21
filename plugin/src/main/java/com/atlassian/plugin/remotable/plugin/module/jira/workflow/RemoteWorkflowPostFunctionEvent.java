package com.atlassian.plugin.remotable.plugin.module.jira.workflow;

import com.atlassian.webhooks.spi.provider.ConsumerKey;
import com.atlassian.webhooks.spi.provider.EventMatcher;
import com.atlassian.webhooks.spi.provider.EventSerializationException;
import com.atlassian.webhooks.spi.provider.EventSerializer;
import com.atlassian.webhooks.spi.provider.EventSerializerFactory;
import org.json.JSONObject;

public class RemoteWorkflowPostFunctionEvent
{
    public static final String REMOTE_WORKFLOW_POST_FUNCTION_EVENT_ID = "remote_workflow_post_function";

    private final ConsumerKey consumerKey;
    private final JSONObject jsonObject;

    public RemoteWorkflowPostFunctionEvent(final ConsumerKey consumerKey, final JSONObject jsonObject)
    {
        this.consumerKey = consumerKey;
        this.jsonObject = jsonObject;
    }

    public String getJson()
    {
        return jsonObject.toString();
    }

    public boolean matches(final ConsumerKey consumerKey)
    {
        return this.consumerKey.getPluginKey().equals(consumerKey.getPluginKey())
                && this.consumerKey.getModuleKey().isPresent()
                && consumerKey.getModuleKey().isPresent()
                && this.consumerKey.getModuleKey().get().equals(consumerKey.getModuleKey().get());
    }

    public static final class FunctionEventMatcher implements EventMatcher<RemoteWorkflowPostFunctionEvent>
    {
        @Override
        public boolean matches(final RemoteWorkflowPostFunctionEvent event, final ConsumerKey consumerKey)
        {
            return event.matches(consumerKey);
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
                public String getJson() throws EventSerializationException
                {
                    return event.getJson();
                }
            };
        }
    }
}
