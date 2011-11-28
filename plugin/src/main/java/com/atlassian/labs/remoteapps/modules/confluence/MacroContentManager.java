package com.atlassian.labs.remoteapps.modules.confluence;

import com.atlassian.confluence.event.events.content.page.PageEvent;
import com.atlassian.confluence.event.events.content.page.PageViewEvent;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.labs.remoteapps.ContentRetrievalException;
import com.google.common.collect.*;
import org.springframework.beans.factory.DisposableBean;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;


/**
 *
 */
public class MacroContentManager implements DisposableBean
{
    private final ConcurrentMap<String,String> cache = new MapMaker().makeMap();
    private final Multimap<String,String> keysByPageId = Multimaps.synchronizedMultimap(LinkedHashMultimap.<String,String>create());
    private final EventPublisher eventPublisher;

    public MacroContentManager(EventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
        this.eventPublisher.register(this);
    }

    @EventListener
    public void onPageEvent(PageEvent pageEvent)
    {
        if (!(pageEvent instanceof PageViewEvent))
        {
            String pageId = pageEvent.getPage().getIdAsString();
            for (String key : keysByPageId.removeAll(pageId))
            {
                cache.remove(key);
            }
        }
    }

    public String getStaticContent(MacroInstance macroInstance) throws ContentRetrievalException
    {
        String key = macroInstance.getHashKey();

        // we don't care about retrieving twice
        String value = cache.get(key);
        if (value == null)
        {
            Map<String,Object> params = Maps.<String,Object>newHashMap(macroInstance.getParameters());
            params.put("body", macroInstance.getBody());
            String pageId = String.valueOf(macroInstance.getPageId());
            params.put("pageId", pageId);

            // todo: handle errors
            value = macroInstance.getLinkOperations().executeGet(macroInstance.getPath(), params);

            cache.put(key, value);
            keysByPageId.put(pageId, key);
        }
        return value;
    }

    @Override
    public void destroy() throws Exception
    {
        eventPublisher.unregister(this);
    }
}
