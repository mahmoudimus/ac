package com.atlassian.labs.remoteapps.plugin.module.confluence;

import com.atlassian.confluence.content.render.xhtml.XhtmlCleaner;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.event.events.content.page.PageEvent;
import com.atlassian.confluence.event.events.content.page.PageViewEvent;
import com.atlassian.confluence.xhtml.api.XhtmlContent;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.labs.remoteapps.plugin.ContentRetrievalException;
import com.atlassian.labs.remoteapps.plugin.RemoteAppAccessorFactory;
import com.atlassian.labs.remoteapps.plugin.util.http.CachingHttpContentRetriever;
import com.atlassian.labs.remoteapps.plugin.util.http.bigpipe.BigPipe;
import com.atlassian.labs.remoteapps.plugin.util.http.bigpipe.BigPipeHttpContentHandler;
import com.atlassian.labs.remoteapps.plugin.util.http.bigpipe.ContentProcessor;
import com.atlassian.labs.remoteapps.plugin.util.http.bigpipe.RequestIdAccessor;
import com.atlassian.renderer.RenderContextOutputType;
import com.atlassian.sal.api.component.ComponentLocator;
import com.atlassian.sal.api.user.UserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

public class MacroContentManager implements DisposableBean
{
    public static final String BIG_PIPE_REQUEST_ID = "__big_pipe_request_id";
    private final EventPublisher eventPublisher;
    private final XhtmlCleaner xhtmlCleaner;
    private final MacroContentLinkParser macroContentLinkParser;
    private final CachingHttpContentRetriever cachingHttpContentRetriever;
    private final BigPipe bigPipe;
    private final UserManager userManager;
    private final XhtmlContent xhtmlUtils;
    private final RemoteAppAccessorFactory remoteAppAccessorFactory;

    private static final Logger log = LoggerFactory.getLogger(MacroContentManager.class);

    public MacroContentManager(EventPublisher eventPublisher,
            CachingHttpContentRetriever cachingHttpContentRetriever,
            MacroContentLinkParser macroContentLinkParser, BigPipe bigPipe, UserManager userManager,
            XhtmlContent xhtmlUtils,
            RemoteAppAccessorFactory remoteAppAccessorFactory)
    {
        this.eventPublisher = eventPublisher;
        this.cachingHttpContentRetriever = cachingHttpContentRetriever;
        this.bigPipe = bigPipe;
        this.userManager = userManager;
        this.xhtmlUtils = xhtmlUtils;
        this.remoteAppAccessorFactory = remoteAppAccessorFactory;
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

        String requestId = macroInstance.getConversionContext().getPropertyAsString(
                BIG_PIPE_REQUEST_ID);
        if (requestId == null)
        {
            requestId = RequestIdAccessor.getRequestId();
            macroInstance.getConversionContext().setProperty(BIG_PIPE_REQUEST_ID, requestId);
        }

        final String author = entity != null ? entity.getLastModifierName() : userManager.getRemoteUsername();

        String contentId = macroInstance.getHashKey();
        final Map<String,String> urlParameters = macroInstance.getUrlParameters(author);
        BigPipeHttpContentHandler httpContentHandler = bigPipe.createContentHandler(requestId, contentId, new ContentProcessor()
        {
            @Override
            public String process(String value)
            {
                value = macroContentLinkParser.parse(
                        macroInstance.getRemoteAppAccessor(), value,
                        urlParameters);

                /*!
               The storage-format XML returned from the Remote App is then scrubbed to ensure any
               JavaScript, CSS, or dangerous HTML elements or attributes aren't present.  This scrubber
               is the same as used in the Confluence editor.
                */
                // todo: do we want to give feedback to the app of what was cleaned?
                String cleanedXhtml = xhtmlCleaner.cleanQuietly(value, macroInstance.getConversionContext());

                try
                {
                    return xhtmlUtils.convertStorageToView(cleanedXhtml, macroInstance.getConversionContext());
                }
                catch (Exception e)
                {
                    log.warn("Unable to convert storage format for app {} with error {}", macroInstance.getRemoteAppAccessor().getKey(), e.getMessage());
                    if (log.isDebugEnabled())
                    {
                        log.debug("Error converting storage format", e);
                    }
                    throw new ContentRetrievalException("Unable to convert storage format to HTML: " + e.getMessage(), e);
                }
            }
        });


        Future<String> response = macroInstance.getRemoteAppAccessor().executeAsyncGet(author,
                macroInstance.getPath(), urlParameters,
                macroInstance.getHeaders(author), httpContentHandler);

        // only render display via big pipe, block for everyone else
        if (RenderContextOutputType.DISPLAY.equals(macroInstance.getConversionContext().getOutputType()))
        {
            return httpContentHandler.getInitialContent();
        }
        else
        {
            try
            {
                response.get();
                httpContentHandler.markCompleted();
                return httpContentHandler.getFinalContent();
            }
            catch (InterruptedException e)
            {
                httpContentHandler.markCompleted();
                throw new ContentRetrievalException(e);
            }
            catch (ExecutionException e)
            {
                httpContentHandler.markCompleted();
                throw new ContentRetrievalException(e.getCause());
            }
        }
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
    2. Flush by <a href="https://remoteapps.jira.com/wiki/display/ARA/Macro+Instance#hash" target="_top">macro key</a>

    These operations are executed through REST resource DELETEs.
     */
    public void clearContentByPluginKey(String pluginKey)
    {
        URI displayUrl = remoteAppAccessorFactory.get(pluginKey).getDisplayUrl();
        cachingHttpContentRetriever.flushCacheByUrlPattern(
                Pattern.compile("^" + displayUrl + "/.*"));
    }

    public void clearContentByInstance(String pluginKey, String instanceKey)
    {
        URI displayUrl = remoteAppAccessorFactory.get(pluginKey).getDisplayUrl();
        cachingHttpContentRetriever.flushCacheByUrlPattern(
                Pattern.compile("^" + displayUrl + "/.*key=" + instanceKey + ".*"));
    }

    @Override
    public void destroy() throws Exception
    {
        eventPublisher.unregister(this);
    }
}
