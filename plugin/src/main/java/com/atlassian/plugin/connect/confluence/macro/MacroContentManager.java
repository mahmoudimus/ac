package com.atlassian.plugin.connect.confluence.macro;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.content.render.xhtml.StorageFormatCleaner;
import com.atlassian.confluence.event.events.content.page.PageEvent;
import com.atlassian.confluence.event.events.content.page.PageViewEvent;
import com.atlassian.confluence.xhtml.api.XhtmlContent;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.connect.plugin.DefaultRemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.uri.IFrameUriBuilder;
import com.atlassian.plugin.connect.plugin.util.UriBuilderUtils;
import com.atlassian.plugin.connect.plugin.util.http.CachingHttpContentRetriever;
import com.atlassian.plugin.connect.plugin.util.http.ContentRetrievalErrors;
import com.atlassian.plugin.connect.plugin.util.http.ContentRetrievalException;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessor;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.util.concurrent.Promise;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import javax.inject.Inject;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * TODO once we drop XML, refactor this to take into account that we no longer support specifying a method type and to
 * make better use of {@link IFrameUriBuilder} and friends.
 */
@ConfluenceComponent
public class MacroContentManager implements DisposableBean
{
    private final EventPublisher eventPublisher;
    private final StorageFormatCleaner xhtmlCleaner;
    private final MacroContentLinkParser macroContentLinkParser;
    private final CachingHttpContentRetriever cachingHttpContentRetriever;
    private final UserManager userManager;
    private final XhtmlContent xhtmlUtils;
    private final DefaultRemotablePluginAccessorFactory remotablePluginAccessorFactory;
    private final TemplateRenderer templateRenderer;
    private final TransactionTemplate transactionTemplate;

    private static final Logger log = LoggerFactory.getLogger(MacroContentManager.class);

    @Inject
    public MacroContentManager(
            EventPublisher eventPublisher,
            CachingHttpContentRetriever cachingHttpContentRetriever,
            MacroContentLinkParser macroContentLinkParser,
            UserManager userManager,
            XhtmlContent xhtmlUtils,
            StorageFormatCleaner cleaner,
            DefaultRemotablePluginAccessorFactory remotablePluginAccessorFactory,
            TemplateRenderer templateRenderer, TransactionTemplate transactionTemplate)
    {
        this.eventPublisher = eventPublisher;
        this.cachingHttpContentRetriever = cachingHttpContentRetriever;
        this.transactionTemplate = transactionTemplate;
        this.userManager = userManager;
        this.xhtmlUtils = xhtmlUtils;
        this.remotablePluginAccessorFactory = remotablePluginAccessorFactory;
        this.templateRenderer = templateRenderer;
        this.eventPublisher.register(this);
        // HACK: Use ComponentLocator until fix for CONFDEV-7103 is available.
        this.xhtmlCleaner = cleaner;
        this.macroContentLinkParser = macroContentLinkParser;
    }

    public String getStaticContent(final MacroInstance macroInstance)
    {
        final UserProfile author = userManager.getRemoteUser();
        final String username = author == null ? "" : author.getUsername();
        final String userKey = author == null ? "" : author.getUserKey().getStringValue();
        final Map<String, String[]> urlParameters = UriBuilderUtils.toMultiValue(macroInstance.getUrlParameters(username, userKey));

        Map<String, String> headers = macroInstance.getHeaders(username, userKey);

        return getStaticContent(macroInstance.method, macroInstance.getPath(), urlParameters, headers,
                macroInstance.conversionContext, macroInstance.getRemotablePluginAccessor());
    }

    public String getStaticContent(HttpMethod method, URI path, Map<String, String[]> urlParameters,
                                   final ConversionContext conversionContext,
                                   final RemotablePluginAccessor accessor)
    {
        return getStaticContent(method, path, urlParameters, ImmutableMap.<String, String>of(), conversionContext, accessor);
    }

    /*!
     The macro content retrieval process details how content is retrieved from the remote plugin
     for display in a macro.  This process does not guarantee the content is only retrieved once
     per cache key as it may be possible for concurrent requests to both prompt a macro content
     retrieval.
    */
    private String getStaticContent(HttpMethod method, URI path, Map<String, String[]> urlParameters,
                                    Map<String, String> headers, final ConversionContext conversionContext,
                                    final RemotablePluginAccessor accessor)
    {
        Promise<String> promise = accessor.executeAsync(method, path, urlParameters, headers);

        try
        {
            // AC-795: synchronous until confluence-related infinite rendering loop is fixed.
            // we are now rendering in the same thread as any sub-rendering macro which "fixes the glitch".
            String remoteXhtml = promise.claim();
            remoteXhtml = macroContentLinkParser.parse(accessor, remoteXhtml, urlParameters);

            /*!
           The storage-format XML returned from the Remotable Plugin is then scrubbed to ensure any
           JavaScript, CSS, or dangerous HTML elements or attributes aren't present.  This scrubber
           is the same as used in the Confluence editor.
            */
            // todo: do we want to give feedback to the app of what was cleaned?
            final String cleanedXhtml = xhtmlCleaner.cleanQuietly(remoteXhtml);
            return transactionTemplate.execute(
                    new TransactionCallback<String>()
                    {
                        @Override
                        public String doInTransaction()
                        {
                            try
                            {
                                return xhtmlUtils.convertStorageToView(cleanedXhtml, conversionContext);
                            }
                            catch (Exception e)
                            {
                                log.warn("Unable to convert storage format for app {} with error {}",
                                        accessor.getKey(), e.getMessage());
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
        }
        catch (ContentRetrievalException e)
        {
            log.error("Could not render macro", e);
            return renderErrors(e.getErrors());
        }
        catch (Exception e)
        {
            log.error("Could not render macro", e);
            return renderErrors(new ContentRetrievalErrors(ImmutableList.of("An unknown error occurred.")));
        }
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
        URI displayUrl = remotablePluginAccessorFactory.get(pluginKey).getBaseUrl();
        cachingHttpContentRetriever.flushCacheByUriPattern(Pattern.compile("^" + displayUrl + "/.*"));
    }

    public void clearContentByInstance(String pluginKey, String instanceKey)
    {
        URI displayUrl = remotablePluginAccessorFactory.get(pluginKey).getBaseUrl();
        cachingHttpContentRetriever.flushCacheByUriPattern(
                Pattern.compile("^" + displayUrl + "/.*key=" + instanceKey + ".*"));
    }

    @Override
    public void destroy() throws Exception
    {
        eventPublisher.unregister(this);
    }

}
