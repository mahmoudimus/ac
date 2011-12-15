package com.atlassian.labs.remoteapps.webhook;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.labs.remoteapps.event.RemoteAppInstalledEvent;
import com.atlassian.labs.remoteapps.event.RemoteAppStartedEvent;
import com.google.common.collect.ImmutableMap;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by IntelliJ IDEA.
 * User: mrdon
 * Date: 15/12/11
 * Time: 10:07 PM
 * To change this template use File | Settings | File Templates.
 */
@Component
public class BaseEventHandler implements DisposableBean
{
    private final WebHookPublisher webHookPublisher;
    private final EventPublisher eventPublisher;

    @Autowired
    public BaseEventHandler(WebHookPublisher webHookPublisher, EventPublisher eventPublisher)
    {
        this.webHookPublisher = webHookPublisher;
        this.eventPublisher = eventPublisher;
        eventPublisher.register(this);
    }

    @EventListener
    public void onRemoteAppInstalled(RemoteAppInstalledEvent event) throws JSONException
    {
        webHookPublisher.publish("remote_app_installed", new MapEventSerializer(event, ImmutableMap.<String, Object>of(
            "key", event.getRemoteAppKey()
        )));
    }

    @EventListener
    public void onRemoteAppStarted(RemoteAppStartedEvent event) throws JSONException
    {
        webHookPublisher.publish("remote_app_started", new MapEventSerializer(event, ImmutableMap.<String, Object>of(
            "key", event.getRemoteAppKey()
        )));
    }

    @Override
    public void destroy() throws Exception
    {
        eventPublisher.unregister(this);
    }
}
