package com.atlassian.labs.remoteapps.webhook;

import com.atlassian.labs.remoteapps.webhook.external.EventSerializerFactory;

/**
 * A registration of a web hook
 */
public class WebHookRegistration
{
    private final String id;
    private Class<?> eventClass;
    private EventSerializerFactory eventSerializerFactory;

    public WebHookRegistration(String id)
    {
        this.id = id;
    }

    public String getId()
    {
        return id;
    }

    public void setEventTrigger(Class<?> eventClass)
    {
        this.eventClass = eventClass;
    }

    public void setEventSerializerFactory(EventSerializerFactory eventSerializerFactory)
    {
        this.eventSerializerFactory = eventSerializerFactory;
    }

    public EventSerializer getEventSerializer(Object event)
    {
        return this.eventSerializerFactory.create(event);
    }

    public Class<?> getEventClass()
    {
        return eventClass;
    }
}
