package com.atlassian.plugin.connect.core.capabilities.event;

import com.atlassian.webhooks.spi.provider.EventSerializationException;
import com.atlassian.webhooks.spi.provider.EventSerializer;

public class ConnectAddonEventSerializer implements EventSerializer
{
    private final Object event;
    private final String data;

    public ConnectAddonEventSerializer(Object event, String data)
    {
        this.event = event;
        this.data = data;
    }

    @Override
    public Object getEvent()
    {
        return event;
    }

    @Override
    public String getWebHookBody() throws EventSerializationException
    {
        return data;
    }
}
