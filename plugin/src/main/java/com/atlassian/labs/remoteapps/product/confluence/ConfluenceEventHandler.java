package com.atlassian.labs.remoteapps.product.confluence;

import com.atlassian.confluence.event.events.content.page.PageCreateEvent;
import com.atlassian.confluence.event.events.content.page.PageUpdateEvent;
import com.atlassian.confluence.json.json.Json;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.labs.remoteapps.webhook.MapEventSerializer;
import com.atlassian.labs.remoteapps.webhook.WebHookPublisher;
import com.google.common.collect.ImmutableMap;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.DisposableBean;

/**
 * Created by IntelliJ IDEA.
 * User: mrdon
 * Date: 15/12/11
 * Time: 9:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConfluenceEventHandler implements DisposableBean
{
    private final EventPublisher eventPublisher;
    private final WebHookPublisher webHookPublisher;

    public ConfluenceEventHandler(EventPublisher eventPublisher, WebHookPublisher webHookPublisher)
    {
        this.eventPublisher = eventPublisher;
        this.webHookPublisher = webHookPublisher;
        eventPublisher.register(this);
    }

    @EventListener
    public void onPageUpdateEvent(PageUpdateEvent event) throws JSONException
    {
        webHookPublisher.publish("page_updated", new MapEventSerializer(event, ImmutableMap.<String, Object>of(
            "pageId", event.getPage().getId(),
            "pageTitle", event.getPage().getTitle(),
            "author", event.getNew().getCreatorName(),
            "content", event.getContent().getBodyAsString()
        )));
    }

    @EventListener
    public void onPageCreateEvent(PageCreateEvent event) throws JSONException
    {
        webHookPublisher.publish("page_created", new MapEventSerializer(event, ImmutableMap.<String, Object>of(
            "pageId", event.getPage().getId(),
            "pageTitle", event.getPage().getTitle(),
            "author", event.getPage().getCreatorName(),
            "content", event.getContent().getBodyAsString()
        )));
    }

    @Override
    public void destroy() throws Exception
    {
        eventPublisher.unregister(this);
    }
}
