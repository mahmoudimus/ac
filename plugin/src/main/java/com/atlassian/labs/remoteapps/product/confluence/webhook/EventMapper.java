package com.atlassian.labs.remoteapps.product.confluence.webhook;

import com.atlassian.confluence.event.events.ConfluenceEvent;

import java.util.Map;

/**
 *
 */
public interface EventMapper
{
    public boolean handles(ConfluenceEvent e);

    public Map<String, Object> toMap(ConfluenceEvent e);
}
