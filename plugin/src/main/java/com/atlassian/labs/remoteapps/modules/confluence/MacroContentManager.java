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

    /*!
     The macro content retrieval process details how content is retrieved from the remote app
     for display in a macro.  This process does not guarantee the content is only retrieved once
     per cache key as it may be possible for concurrent requests to both prompt a macro content
     retrieval.
    */
    public String getStaticContent(final MacroInstance macroInstance) throws ContentRetrievalException
    {
        ContentEntityObject entity = macroInstance.getEntity();
        final String key = macroInstance.getHashKey();
        final String pluginKey = ((NonAppLinksApplicationType) macroInstance.getLinkOperations().get().getType()).getId().get();
        final String spaceKey = entity instanceof SpaceContentEntityObject ? ((SpaceContentEntityObject)entity).getSpaceKey() : null;
        final String pageId = entity.getIdAsString();

        /*!
        First, the database cache is checked for the content matching the
        <a href="Macro+Instance#hash">macro key</a>.
        */
        SavedMacroInstance instance = getFromBandana(pluginKey, spaceKey, pageId, key);

        /*!
        ### Case: Cached Content Not Found

        If cached content cannot be found, the HTTP client is instructed to retrieve the content via
        a synchronous GET request.  This request will be made with a
        <a href="Macro+Instance#url-parameters">set of query parameters</a> to assist the Remote
        App in returning the correct content.

        The HTTP client is configured to have a three second connection timeout and an 5 second
        request timeout to enforce minimal performance.  It is also configured to have an in-memory
        cache that respects the HTTP 1.1 caching headers from the GET response.  This gives the
        remote app more control over how the content is cached and refreshed.
        */
        if (instance == null)
        {
            String value = macroInstance.getLinkOperations().executeGet(entity.getLastModifierName(), macroInstance.getPath(), macroInstance.getUrlParameters());


            value = macroContentLinkParser.parse(macroInstance.getLinkOperations().get(), value,
                    macroInstance.getUrlParameters());

            /*!
            The storage-format XML returned from the Remote App is then scrubbed to ensure any
            JavaScript, CSS, or dangerous HTML elements or attributes aren't present.  This scrubber
            is the same as used in the Confluence editor.
             */
            // todo: do we want to give feedback to the app of what was cleaned?
            value = xhtmlCleaner.cleanQuietly(value, macroInstance.getConversionContext());

            /*!
            Finally, the newly retrieved content is persisted, but only if the macro isn't being
            rendered as part of the preview operation.
             */
            instance = createSavedInstance(value);
            if (!RenderContextOutputType.PREVIEW.equals(macroInstance.getConversionContext().getOutputType()))
            {
                saveToBandana(pluginKey, spaceKey, pageId, key, instance);
            }
        /*!
        ### Case: Expired Cached Content Found

        If cached content has been found, but has expired, it will need to be retrieved again, but
        only asynchronously after the macro has been rendered with the expired content.  The
        expiry of one hour is necessary to ensure a poorly-configured Remote App can't configure
        the macro content to be retrieved on every request.
         */
        } else if (instance.getExpiry() < System.currentTimeMillis())
        {
            /*!
            The content is retrieved asynchronously to ensure the expired content won't cause the
            macro rendering, and therefore the page rendering, won't block.
             */
            macroInstance.getLinkOperations().executeGetAsync(entity.getLastModifierName(), macroInstance.getPath(),
                                                              macroInstance.getUrlParameters(),
                new HttpContentHandler()
                {
                    /*!
                    When the content is retrieved from the async call, the content is scrubbed
                    like normal and persisted in the cache.
                     */
                    @Override
                    public void onSuccess(String content)
                    {
                        String value = xhtmlCleaner.cleanQuietly(content, macroInstance.getConversionContext());
                        saveToBandana(pluginKey, spaceKey, pageId, key, createSavedInstance(value));
                    }

                    /*!
                    If there is an error with the async retrieval, only a warning is written to the
                    logs.  Eventually, this error will be made available to the Remote App dev
                    somehow.
                     */
                    @Override
                    public void onError(ContentRetrievalException ex)
                    {
                        // todo: this should be made available to the app dev somehow
                        log.warn("Unable to refresh macro content for app '{}' due to: '{}'", pluginKey, ex.getMessage());

                    }
                }
            );
        }
        /*!
        If cached content has been found and it isn't expired, the storage-format XML is returned
        to the macro.
         */
        return instance.getValue();
    }

    /*!
    ## Cache Flushing

    There are several ways the cache can be flushed to force a new retrieval of macro content.
    */

    /*!
    ### Automatic Flushes

    Macro content is automatically flushed in response to any page event other than a view.  This
    includes events like new pages, edited pages, and even re-ordered pages.
     */
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

    /*!
    ### Explicit Flushes

    Remote Apps provides two operations that allow a Remote App to explicitly flush macro content
    flushes:

    1. Flush by app key
    2. Flush by <a href="Macro+Instance#hash">macro key</a>

    These operations are executed through REST resource DELETEs.
     */
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

    /*!-helper methods */
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
