package com.atlassian.plugin.connect.plugin.module.confluence;

import com.atlassian.confluence.content.render.xhtml.StorageFormatCleaner;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.event.events.content.page.PageEvent;
import com.atlassian.confluence.event.events.content.page.PageViewEvent;
import com.atlassian.confluence.xhtml.api.XhtmlContent;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.connect.api.service.http.bigpipe.BigPipeManager;
import com.atlassian.plugin.connect.plugin.DefaultRemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.plugin.util.http.CachingHttpContentRetriever;
import com.atlassian.plugin.connect.plugin.util.http.ContentRetrievalErrors;
import com.atlassian.plugin.connect.plugin.util.http.ContentRetrievalException;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.util.concurrent.Promise;
import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.*;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

public class MacroContentManager implements DisposableBean
{
    private final EventPublisher eventPublisher;
    private final StorageFormatCleaner xhtmlCleaner;
    private final MacroContentLinkParser macroContentLinkParser;
    private final CachingHttpContentRetriever cachingHttpContentRetriever;
    private final BigPipeManager bigPipeManager;
    private final UserManager userManager;
    private final XhtmlContent xhtmlUtils;
    private final DefaultRemotablePluginAccessorFactory remotablePluginAccessorFactory;
    private final TemplateRenderer templateRenderer;
    private final TransactionTemplate transactionTemplate;

    private static final Logger log = LoggerFactory.getLogger(MacroContentManager.class);

    public MacroContentManager(
            EventPublisher eventPublisher,
            CachingHttpContentRetriever cachingHttpContentRetriever,
            MacroContentLinkParser macroContentLinkParser,
            BigPipeManager bigPipeManager,
            UserManager userManager,
            XhtmlContent xhtmlUtils,
            StorageFormatCleaner cleaner,
            DefaultRemotablePluginAccessorFactory remotablePluginAccessorFactory,
            TemplateRenderer templateRenderer, TransactionTemplate transactionTemplate)
    {
        this.eventPublisher = eventPublisher;
        this.cachingHttpContentRetriever = cachingHttpContentRetriever;
        this.transactionTemplate = transactionTemplate;
        this.bigPipeManager = checkNotNull(bigPipeManager);
        this.userManager = userManager;
        this.xhtmlUtils = xhtmlUtils;
        this.remotablePluginAccessorFactory = remotablePluginAccessorFactory;
        this.templateRenderer = templateRenderer;
        this.eventPublisher.register(this);
        // HACK: Use ComponentLocator until fix for CONFDEV-7103 is available.
        this.xhtmlCleaner = cleaner;
        this.macroContentLinkParser = macroContentLinkParser;
    }

    // AC-795: remove this cache when bigpipe- and confluence-related infinite rendering loop is fixed
    private static class FiniteLinkedHashMap<K,V> extends LinkedHashMap<K,V>
    {
        private final int maxSize;

        FiniteLinkedHashMap(final int maxSize)
        {
            this.maxSize = maxSize;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K,V> eldest)
        {
            boolean evict = size() > maxSize;

            if (evict)
            {
                log.warn("Evicting eldest cache entry because the cache is at its maximum size of {}: '{}'", maxSize, eldest.getKey().toString());
            }

            return evict;
        }
    }

    // AC-795: remove this cache when bigpipe- and confluence-related infinite rendering loop is fixed
    private static class CurrentMacroRequestCache
    {
        private static final int MAX_CACHE_SIZE = 1000;
        private static CurrentMacroRequestCache instance = new CurrentMacroRequestCache();
        private Map<String, Boolean> macroRequestKeys = Collections.synchronizedMap(new FiniteLinkedHashMap<String, Boolean>(MAX_CACHE_SIZE));

        private CurrentMacroRequestCache() {} // no externally constructed instances

        boolean contains(String macroRequestKey)
        {
            return macroRequestKeys.containsKey(macroRequestKey);
        }

        void cache(String macroRequestKey)
        {
            log.warn("Caching macro request key: '{}'", macroRequestKey);
            macroRequestKeys.put(macroRequestKey, true);
        }

        void evict(String macroRequestKey)
        {
            log.warn("Evicting macro request key: '{}'", macroRequestKey);
            macroRequestKeys.remove(macroRequestKey);
        }

        static CurrentMacroRequestCache getInstance()
        {
            return instance;
        }

        static String constructKey(HttpMethod method, URI path, Map<String, String> urlParameters, Map<String, String> headers)
        {
            StringBuilder sb = new StringBuilder();
            sb.append(method.name())
              .append(path)
              .append(constructKeyFromMap(urlParameters))
              .append(constructKeyFromMap(headers));
            return sb.toString();
        }

        private static String constructKeyFromMap(Map<String, String> urlParameters)
        {
            StringBuilder sb = new StringBuilder();
            List<String> urlParamKeys = new ArrayList<String>(urlParameters.keySet());
            Collections.sort(urlParamKeys);

            for (String urlParamKey : urlParamKeys)
            {
                sb.append(urlParamKey).append(urlParameters.get(urlParamKey));
            }

            return sb.toString();
        }
    }

    /*!
     The macro content retrieval process details how content is retrieved from the remote plugin
     for display in a macro.  This process does not guarantee the content is only retrieved once
     per cache key as it may be possible for concurrent requests to both prompt a macro content
     retrieval.
    */
    public String getStaticContent(final MacroInstance macroInstance) throws ContentRetrievalException
    {
        ContentEntityObject entity = macroInstance.getEntity();

        final UserProfile author = getUserToRenderMacroAs(entity);
        final String username = author == null ? "" : author.getUsername();
        final String userKey = author == null ? "" : author.getUserKey().getStringValue();

        final Map<String, String> urlParameters = macroInstance.getUrlParameters(username, userKey);

        Map<String, String> headers = macroInstance.getHeaders(username, userKey);
        String macroContentRequestKey = CurrentMacroRequestCache.constructKey(macroInstance.method,
                macroInstance.getPath(), urlParameters, headers);
        CurrentMacroRequestCache currentMacroRequestCache = CurrentMacroRequestCache.getInstance(); // AC-795: remove this cache when bigpipe- and confluence-related infinite rendering loop is fixed

        if (currentMacroRequestCache.contains(macroContentRequestKey))
        {
            log.warn("This macro request is already in progress so I'm returning nothing: '{}'", macroContentRequestKey);
            return "";
        }

        currentMacroRequestCache.cache(macroContentRequestKey);
        Promise<String> promise = macroInstance.getRemotablePluginAccessor().executeAsync(macroInstance.method,
                macroInstance.getPath(), urlParameters, headers)
                .fold(new ContentHandlerFailFunction(templateRenderer),
                        new HtmlToSafeHtmlFunction(macroInstance, urlParameters, macroContentLinkParser, xhtmlCleaner,
                                xhtmlUtils, transactionTemplate));
        
        try
        {
            return promise.claim(); // AC-795: synchronous until bigpipe- and confluence-related infinite rendering loop is fixed
        }
        catch (RuntimeException e)
        {
            log.debug("Exception retrieving content", e);
            throw new ContentRetrievalException(Throwables.getRootCause(e));
        }
        finally
        {
            currentMacroRequestCache.evict(macroContentRequestKey);
        }
    }

    private UserProfile getUserToRenderMacroAs(ContentEntityObject entity)
    {
        if (entity != null && !StringUtils.isBlank(entity.getLastModifierName()))
        {
            return userManager.getUserProfile(entity.getLastModifierName());
        }
        return userManager.getRemoteUser();
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
            cachingHttpContentRetriever.flushCacheByUriPattern(Pattern.compile(".*page_id=" + pageId + ".*"));
        }
    }

    /*!
    ### Explicit Flushes

    Remotable Plugins provides two operations that allow a Remotable Plugin to explicitly flush macro content
    flushes:

    1. Flush by app key
    2. Flush by <a href="https://remoteapps.jira.com/wiki/display/ARA/Macro+Instance#hash" target="_top">macro key</a>

    These operations are executed through REST resource DELETEs.
     */
    public void clearContentByPluginKey(String pluginKey)
    {
        URI displayUrl = remotablePluginAccessorFactory.get(pluginKey).getDisplayUrl();
        cachingHttpContentRetriever.flushCacheByUriPattern(Pattern.compile("^" + displayUrl + "/.*"));
    }

    public void clearContentByInstance(String pluginKey, String instanceKey)
    {
        URI displayUrl = remotablePluginAccessorFactory.get(pluginKey).getDisplayUrl();
        cachingHttpContentRetriever.flushCacheByUriPattern(
                Pattern.compile("^" + displayUrl + "/.*key=" + instanceKey + ".*"));
    }

    @Override
    public void destroy() throws Exception
    {
        eventPublisher.unregister(this);
    }

    private static final class ContentHandlerFailFunction implements Function<Throwable, String>
    {
        private TemplateRenderer templateRenderer;

        private ContentHandlerFailFunction(TemplateRenderer templateRenderer)
        {
            this.templateRenderer = checkNotNull(templateRenderer);
        }

        @Override
        public String apply(Throwable t)
        {
            final ContentRetrievalErrors errors;
            if (t instanceof ContentRetrievalException)
            {
                errors = ((ContentRetrievalException) t).getErrors();
            }
            else
            {
                errors = new ContentRetrievalErrors(ImmutableList.of("An unknown error occurred."));
                log.warn("An unknown error occurred rendering the macro", t);
            }

            return renderErrors(errors);
        }

        private String renderErrors(ContentRetrievalErrors errors)
        {
            try
            {
                final Writer writer = new StringWriter();
                templateRenderer.render(
                        "/velocity/macro/errors.vm",
                        ImmutableMap.<String, Object>of("errors", errors),
                        writer);
                return writer.toString();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    private static class HtmlToSafeHtmlFunction implements Function<String, String>
    {
        private final MacroInstance macroInstance;
        private final Map<String, String> urlParameters;
        private final MacroContentLinkParser macroContentLinkParser;
        private final StorageFormatCleaner xhtmlCleaner;
        private final XhtmlContent xhtmlUtils;
        private final TransactionTemplate transactionTemplate;

        public HtmlToSafeHtmlFunction(MacroInstance macroInstance, Map<String, String> urlParameters,
                                      MacroContentLinkParser macroContentLinkParser, StorageFormatCleaner xhtmlCleaner,
                                      XhtmlContent xhtmlUtils, TransactionTemplate transactionTemplate)
        {
            this.macroInstance = macroInstance;
            this.urlParameters = urlParameters;
            this.macroContentLinkParser = macroContentLinkParser;
            this.xhtmlCleaner = xhtmlCleaner;
            this.xhtmlUtils = xhtmlUtils;
            this.transactionTemplate = transactionTemplate;
        }

        @Override
        public String apply(String value)
        {
            value = macroContentLinkParser.parse(macroInstance.getRemotablePluginAccessor(), value, urlParameters);

            /*!
           The storage-format XML returned from the Remotable Plugin is then scrubbed to ensure any
           JavaScript, CSS, or dangerous HTML elements or attributes aren't present.  This scrubber
           is the same as used in the Confluence editor.
            */
            // todo: do we want to give feedback to the app of what was cleaned?
            final String cleanedXhtml = xhtmlCleaner.cleanQuietly(value, macroInstance.getConversionContext());
            String content = transactionTemplate.execute(
                    new TransactionCallback<String>() {
                        @Override
                        public String doInTransaction()
                        {
                            try
                            {
                                return xhtmlUtils.convertStorageToView(cleanedXhtml, macroInstance.getConversionContext());
                            }
                            catch (Exception e)
                            {
                                log.warn("Unable to convert storage format for app {} with error {}",
                                        macroInstance.getRemotablePluginAccessor().getKey(), e.getMessage());
                                if (log.isDebugEnabled())
                                {
                                    log.debug("Error converting storage format", e);
                                }
                                throw new ContentRetrievalException(
                                        "Unable to convert storage format to HTML: " + e.getMessage(), e);
                            }
                        }
                    }
            );

            return content;
        }
    }
}
