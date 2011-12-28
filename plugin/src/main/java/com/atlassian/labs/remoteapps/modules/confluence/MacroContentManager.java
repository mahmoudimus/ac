package com.atlassian.labs.remoteapps.modules.confluence;

import com.atlassian.applinks.spi.application.NonAppLinksApplicationType;
import com.atlassian.bandana.BandanaManager;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.event.events.content.page.PageEvent;
import com.atlassian.confluence.event.events.content.page.PageViewEvent;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.setup.bandana.ConfluenceBandanaContext;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.labs.remoteapps.ContentRetrievalException;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.DisposableBean;

import java.util.Map;
import java.util.regex.Pattern;

import static com.google.common.collect.Sets.newHashSet;


/**
 *
 */
public class MacroContentManager implements DisposableBean
{
    private final EventPublisher eventPublisher;
    private final BandanaManager bandanaManager;
    private final SpaceManager spaceManager;
    private final PageManager pageManager;

    public MacroContentManager(EventPublisher eventPublisher, BandanaManager bandanaManager, SpaceManager spaceManager, PageManager pageManager)
    {
        this.eventPublisher = eventPublisher;
        this.bandanaManager = bandanaManager;
        this.spaceManager = spaceManager;
        this.pageManager = pageManager;
        this.eventPublisher.register(this);
    }

    @EventListener
    public void onPageEvent(PageEvent pageEvent)
    {
        if (!(pageEvent instanceof PageViewEvent))
        {
            clearFromBandanaByPageId(pageEvent.getPage().getSpaceKey(), pageEvent.getPage().getIdAsString());
        }
    }

    public void clearContentByPageId(long pageId)
    {
        Page page = pageManager.getPage(pageId);
        clearFromBandanaByPageId(page.getSpaceKey(), page.getIdAsString());
    }

    public void clearContentByPluginKey(String pluginKey)
    {
        clearFromBandanaByPluginKey(pluginKey);
    }

    public void clearContentByKey(long pageId, String key)
    {
        Page page = pageManager.getPage(pageId);
        clearFromBandanaByPageIdAndKey(page.getSpaceKey(), page.getIdAsString(), key);
    }

    public String getStaticContent(MacroInstance macroInstance) throws ContentRetrievalException
    {
        ContentEntityObject entity = macroInstance.getEntity();
        String key = macroInstance.getHashKey();
        String pluginKey = ((NonAppLinksApplicationType) macroInstance.getLinkOperations().get().getType()).getId().get();
        String spaceKey = entity instanceof Page ? ((Page)entity).getSpaceKey() : null;
        String pageId = entity.getIdAsString();

        // we don't care about retrieving twice
        String value = getFromBandana(pluginKey, spaceKey, pageId, key);
        if (value == null)
        {
            Map<String,Object> params = Maps.<String,Object>newHashMap(macroInstance.getParameters());
            params.put("body", macroInstance.getBody());
            params.put("key", key);
            params.put("pageTitle", entity.getTitle());
            params.put("pageId", pageId);

            // todo: handle errors
            value = macroInstance.getLinkOperations().executeGet(entity.getLastModifierName(), macroInstance.getPath(), params);

            saveToBandana(pluginKey, spaceKey, pageId, key, value);
        }
        return value;
    }

    private void saveToBandana(String pluginKey, String spaceKey, String pageId, String key, String value)
    {
        bandanaManager.setValue(new ConfluenceBandanaContext(spaceKey), generateCacheKey(pluginKey, pageId, key), value);
    }

    private String getFromBandana(String pluginKey, String spaceKey, String pageId, String key)
    {
        return (String) bandanaManager.getValue(new ConfluenceBandanaContext(spaceKey), generateCacheKey(pluginKey, pageId, key));
    }

    private void clearFromBandanaByPageId(String spaceKey, String pageId)
    {
        Pattern PAGE_ID_FILTER = Pattern.compile("remoteMacro__[0-9]+__" + pageId + "__[-0-9]+");
        ConfluenceBandanaContext context = new ConfluenceBandanaContext(spaceKey);
        for (String key : newHashSet(bandanaManager.getKeys(context)))
        {
            if (PAGE_ID_FILTER.matcher(key).matches())
            {
                bandanaManager.removeValue(context, key);
            }
        }
    }

    private void clearFromBandanaByPageIdAndKey(String spaceKey, String pageId, String key)
    {
        Pattern PAGE_ID_FILTER = Pattern.compile("remoteMacro__[0-9]+__" + pageId + "__" + key);
        ConfluenceBandanaContext context = new ConfluenceBandanaContext(spaceKey);
        for (String bandanaKey : newHashSet(bandanaManager.getKeys(context)))
        {
            if (PAGE_ID_FILTER.matcher(bandanaKey).matches())
            {
                bandanaManager.removeValue(context, bandanaKey);
            }
        }
    }

    private void clearFromBandanaByPluginKey(String pluginKey)
    {
        Pattern PLUGIN_KEY_FILTER = Pattern.compile("remoteMacro__" + pluginKey.hashCode() + "__[0-9]+__[-0-9]+");
        for (Space space : spaceManager.getAllSpaces())
        {
            ConfluenceBandanaContext context = new ConfluenceBandanaContext(space.getKey());
            for (String key : newHashSet(bandanaManager.getKeys(context)))
            {
                if (PLUGIN_KEY_FILTER.matcher(key).matches())
                {
                    bandanaManager.removeValue(context, key);
                }
            }
        }
    }

    private String generateCacheKey(String pluginKey, String pageId, String key)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("remoteMacro__")
                .append(pluginKey.hashCode()).append("__")
                .append(pageId).append("__")
                .append(key);
        return sb.toString();
    }

    @Override
    public void destroy() throws Exception
    {
        eventPublisher.unregister(this);
    }
}
