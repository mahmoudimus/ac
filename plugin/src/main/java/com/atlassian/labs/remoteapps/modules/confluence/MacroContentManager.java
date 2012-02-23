package com.atlassian.labs.remoteapps.modules.confluence;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.spi.application.NonAppLinksApplicationType;
import com.atlassian.bandana.BandanaManager;
import com.atlassian.confluence.content.render.xhtml.XhtmlCleaner;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.core.SpaceContentEntityObject;
import com.atlassian.confluence.event.events.content.page.PageEvent;
import com.atlassian.confluence.event.events.content.page.PageViewEvent;
import com.atlassian.confluence.setup.bandana.ConfluenceBandanaContext;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.labs.remoteapps.ApplicationLinkAccessor;
import com.atlassian.labs.remoteapps.ContentRetrievalException;
import com.atlassian.labs.remoteapps.util.http.CachingHttpContentRetriever;
import com.atlassian.labs.remoteapps.util.http.HttpContentHandler;
import com.atlassian.renderer.RenderContextOutputType;
import com.atlassian.sal.api.component.ComponentLocator;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static com.google.common.collect.Sets.newHashSet;
import static org.apache.commons.lang.Validate.notNull;


/**
 * Macro content is cached as follows:
 * <ol>
 *     <li>Bandana is checked.  If found and hasn't expired (one hour), the content is returned.  If has expired, the content
 *     is returned, but the content is refreshed asynchronously.</li>
 *     <li>When retrieving content either due to no cache or expired cache, an in-memory cache maintained by http client
 *     is used that respects HTTP 1.1 cache headers.  This is managed by
 *     {@link com.atlassian.labs.remoteapps.util.http.CachingHttpContentRetriever}.  This gives the macro control over how
 *     their content is cached but without allowing the content to expire too often.</li>
 * </ol>
 */
public class MacroContentManager implements DisposableBean
{
    private static final int HOUR_IN_MILLIS = 1000 * 60 * 60;
    private final EventPublisher eventPublisher;
    private final BandanaManager bandanaManager;
    private final SpaceManager spaceManager;
    private final XhtmlCleaner xhtmlCleaner;
    private final MacroContentLinkParser macroContentLinkParser;
    private final CachingHttpContentRetriever cachingHttpContentRetriever;
    private final ApplicationLinkAccessor applicationLinkAccessor;

    private static final Logger log = LoggerFactory.getLogger(MacroContentManager.class);

    public MacroContentManager(EventPublisher eventPublisher, BandanaManager bandanaManager, SpaceManager spaceManager,
                               CachingHttpContentRetriever cachingHttpContentRetriever,
                               ApplicationLinkAccessor applicationLinkAccessor,
                               MacroContentLinkParser macroContentLinkParser)
    {
        this.eventPublisher = eventPublisher;
        this.bandanaManager = bandanaManager;
        this.spaceManager = spaceManager;
        this.cachingHttpContentRetriever = cachingHttpContentRetriever;
        this.applicationLinkAccessor = applicationLinkAccessor;
        this.eventPublisher.register(this);
        // HACK: Use ComponentLocator until fix for CONFDEV-7103 is available.
        this.xhtmlCleaner = ComponentLocator.getComponent(XhtmlCleaner.class);
        this.macroContentLinkParser = macroContentLinkParser;
    }

    @EventListener
    public void onPageEvent(PageEvent pageEvent)
    {
        if (!(pageEvent instanceof PageViewEvent))
        {
            String pageId = pageEvent.getPage().getIdAsString();
            clearFromBandanaByPageId(pageEvent.getPage().getSpaceKey(), pageId);
            cachingHttpContentRetriever.flushCacheByUrlPattern(
                    Pattern.compile(".*pageId=" + pageId + ".*")
                    );
        }
    }

    public void clearContentByPluginKey(String pluginKey)
    {
        clearFromBandanaByPluginKey(pluginKey);
        ApplicationLink link = applicationLinkAccessor.getApplicationLink(pluginKey);
        cachingHttpContentRetriever.flushCacheByUrlPattern(
                Pattern.compile("^" + link.getDisplayUrl() + "/.*"));
    }

    public void clearContentByInstance(String pluginKey, String instanceKey)
    {
        clearFromBandanaByPluginKeyAndInstance(pluginKey, instanceKey);
        ApplicationLink link = applicationLinkAccessor.getApplicationLink(pluginKey);
        cachingHttpContentRetriever.flushCacheByUrlPattern(
                Pattern.compile("^" + link.getDisplayUrl() + "/.*key=" + instanceKey + ".*"));
    }

    public String getStaticContent(final MacroInstance macroInstance) throws ContentRetrievalException
    {
        ContentEntityObject entity = macroInstance.getEntity();
        final String key = macroInstance.getHashKey();
        final String pluginKey = ((NonAppLinksApplicationType) macroInstance.getLinkOperations().get().getType()).getId().get();
        final String spaceKey = entity instanceof SpaceContentEntityObject ? ((SpaceContentEntityObject)entity).getSpaceKey() : null;
        final String pageId = entity.getIdAsString();

        // we don't care about retrieving twice
        SavedMacroInstance instance = getFromBandana(pluginKey, spaceKey, pageId, key);

        // we only want to try to refresh macro content after expiry to ensure a badly-configured app can't disable caching
        if (instance == null)
        {
            String value = macroInstance.getLinkOperations().executeGet(entity.getLastModifierName(), macroInstance.getPath(), macroInstance.getUrlParameters());


            value = macroContentLinkParser.parse(macroInstance.getLinkOperations().get(), value,
                    macroInstance.getUrlParameters());

            // todo: do we want to give feedback to the app of what was cleaned?
            value = xhtmlCleaner.cleanQuietly(value, macroInstance.getConversionContext());
            instance = createSavedInstance(value);
            if (!RenderContextOutputType.PREVIEW.equals(macroInstance.getConversionContext().getOutputType()))
            {
                saveToBandana(pluginKey, spaceKey, pageId, key, instance);
            }
        } else if (instance.getExpiry() < System.currentTimeMillis())
        { 
            // back fill cache for next call, but don't block the current one
            macroInstance.getLinkOperations().executeGetAsync(entity.getLastModifierName(), macroInstance.getPath(),
                                                              macroInstance.getUrlParameters(),
                new HttpContentHandler()
                {
                    @Override
                    public void onSuccess(String content)
                    {
                        String value = xhtmlCleaner.cleanQuietly(content, macroInstance.getConversionContext());
                        saveToBandana(pluginKey, spaceKey, pageId, key, createSavedInstance(value));
                    }

                    @Override
                    public void onError(ContentRetrievalException ex)
                    {
                        // todo: this should be made available to the app dev somehow
                        log.warn("Unable to refresh macro content for app '{}' due to: '{}'", pluginKey, ex.getMessage());

                    }
                }
            );
        }
        return instance.getValue();
    }

    private SavedMacroInstance saveToBandana(String pluginKey, String spaceKey, String pageId, String key, SavedMacroInstance savedMacroInstance)
    {
        bandanaManager.setValue(new ConfluenceBandanaContext(spaceKey),
                                generateCacheKey(pluginKey, pageId, key),
                                savedMacroInstance.toJson());
        return savedMacroInstance;
    }

    private SavedMacroInstance createSavedInstance(String value)
    {
        long expiry = System.currentTimeMillis() + HOUR_IN_MILLIS;
        return new SavedMacroInstance(value, expiry);
    }

    private SavedMacroInstance getFromBandana(String pluginKey, String spaceKey, String pageId, String key)
    {
        String value = (String) bandanaManager.getValue(new ConfluenceBandanaContext(spaceKey), generateCacheKey(pluginKey, pageId, key));
        return value != null ? new SavedMacroInstance(value) : null;
    }

    private void clearFromBandanaByPageId(String spaceKey, String pageId)
    {
        Pattern PAGE_ID_FILTER = Pattern.compile("remoteMacro__[-0-9]+__" + pageId + "__[-0-9]+");
        ConfluenceBandanaContext context = new ConfluenceBandanaContext(spaceKey);
        for (String key : newHashSet(bandanaManager.getKeys(context)))
        {
            if (PAGE_ID_FILTER.matcher(key).matches())
            {
                bandanaManager.removeValue(context, key);
            }
        }
    }

    private void clearFromBandanaByPluginKeyAndInstance(String pluginKey, String instanceKey)
    {
        Pattern PLUGIN_KEY_FILTER = Pattern.compile("remoteMacro__" + pluginKey.hashCode() + "__[0-9]+__" + instanceKey);
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
