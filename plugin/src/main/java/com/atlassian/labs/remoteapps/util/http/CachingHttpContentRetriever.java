package com.atlassian.labs.remoteapps.util.http;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.labs.remoteapps.ContentRetrievalException;
import com.atlassian.labs.remoteapps.OAuthLinkManager;
import com.atlassian.labs.remoteapps.RetrievalTimeoutException;
import com.atlassian.labs.remoteapps.util.uri.Uri;
import com.atlassian.labs.remoteapps.util.uri.UriBuilder;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.util.concurrent.ThreadFactories;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.AbortableHttpRequest;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.conn.ClientConnectionRequest;
import org.apache.http.conn.ConnectionReleaseTrigger;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpAsyncClient;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;
import org.apache.http.impl.nio.client.DefaultHttpAsyncClient;
import org.apache.http.impl.nio.conn.AsyncSchemeRegistryFactory;
import org.apache.http.impl.nio.conn.PoolingClientAsyncConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.nio.reactor.IOReactorExceptionHandler;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ProxySelector;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import static java.util.Collections.singletonList;

/**
 * This is a public component
 */
public class CachingHttpContentRetriever implements DisposableBean, HttpContentRetriever
{
    private final FlushableHttpCacheStorage httpCacheStorage;
    CachingHttpAsyncClient httpClient;
    private final OAuthLinkManager oAuthLinkManager;
    private final UserManager userManager;
    private final Logger log = LoggerFactory.getLogger(CachingHttpContentRetriever.class);
    private final RequestTimeoutKiller requestKiller;

    public CachingHttpContentRetriever(OAuthLinkManager oAuthLinkManager, UserManager userManager,
            PluginRetrievalService pluginRetrievalService, RequestTimeoutKiller requestKiller)
    {
        this.oAuthLinkManager = oAuthLinkManager;
        this.userManager = userManager;
        this.requestKiller = requestKiller;
        CacheConfig cacheConfig = new CacheConfig();
        cacheConfig.setMaxCacheEntries(1000);
        cacheConfig.setSharedCache(false);
        cacheConfig.setMaxObjectSize(8192L);

        DefaultHttpAsyncClient client;
        try
        {
            IOReactorConfig ioReactorConfig = new IOReactorConfig();
            ioReactorConfig.setIoThreadCount(10);
            ioReactorConfig.setSelectInterval(100);
            ioReactorConfig.setInterestOpQueued(true);
            DefaultConnectingIOReactor reactor = new DefaultConnectingIOReactor(
                    ioReactorConfig,
                    ThreadFactories.namedThreadFactory("ra-http-retriever",
                            ThreadFactories.Type.DAEMON));
            reactor.setExceptionHandler(new IOReactorExceptionHandler()
            {
                @Override
                public boolean handle(IOException ex)
                {
                    log.error("IO exception in reactor", ex);
                    return false;
                }

                @Override
                public boolean handle(RuntimeException ex)
                {
                    log.error("Fatal runtime error", ex);
                    return false;
                }
            });
            client = new DefaultHttpAsyncClient(new PoolingClientAsyncConnectionManager(reactor, AsyncSchemeRegistryFactory.createDefault(), 3, TimeUnit.SECONDS)
            {
                @Override
                protected void finalize() throws Throwable
                {
                    // prevent the PoolingClientAsyncConnectionManager from logging - this causes exceptions due to
                    // the ClassLoader probably having been removed when the plugin shuts down.  Added a
                    // PluginEventListener to make sure the shutdown method is called while the plugin classloader
                    // is still active.
                }
            });
        }
        catch (IOReactorException e)
        {
            throw new RuntimeException("Reactor not set up correctly", e);
        }

        HttpParams params = client.getParams();
        HttpProtocolParams.setUserAgent(params, "Atlassian-RemoteApps/" + pluginRetrievalService.getPlugin().getPluginInformation().getVersion());

        HttpConnectionParams.setConnectionTimeout(params, 3 * 1000);
        HttpConnectionParams.setSoTimeout(params, 7 * 1000);
        HttpConnectionParams.setSocketBufferSize(params, 8 * 1024);
        HttpConnectionParams.setTcpNoDelay(params, true);

        ProxySelectorAsyncRoutePlanner routePlanner = new ProxySelectorAsyncRoutePlanner(
                client.getConnectionManager().getSchemeRegistry(),
                ProxySelector.getDefault());
        client.setRoutePlanner(routePlanner);


        httpCacheStorage = new FlushableHttpCacheStorage(cacheConfig);
        httpClient = new CachingHttpAsyncClient(client, httpCacheStorage, cacheConfig);
        httpClient.start();
    }

    @Override
    public void flushCacheByUrlPattern(Pattern urlPattern)
    {
        httpCacheStorage.flushByUrlPattern(urlPattern);
    }

    @Override
    public Future<String> getAsync(final ApplicationLink link, final String remoteUsername,
            final String originalUrl,
            final Map<String, String> parameters, final Map<String, String> headers,
            final HttpContentHandler handler)
    {
        return makeCall(link, remoteUsername, originalUrl, parameters, headers, handler);
    }

    @Override
    public String get(ApplicationLink link, String remoteUsername, String originalUrl, Map<String, String> parameters) throws
                                                                                                              ContentRetrievalException
    {
        final AtomicReference<String> successResult = new AtomicReference<String>();
        final AtomicReference<ContentRetrievalException> exceptionRef = new AtomicReference<ContentRetrievalException>();
        try
        {
            makeCall(link, remoteUsername, originalUrl, parameters,
                    Collections.<String,String>emptyMap(), new HttpContentHandler()
            {
                @Override
                public void onSuccess(String content)
                {
                    successResult.set(content);
                }

                @Override
                public void onError(ContentRetrievalException ex)
                {
                    exceptionRef.set(ex);
                }
            }).get();
        }
        catch (InterruptedException e)
        {
            // ignore
        }
        catch (ExecutionException e)
        {
            throw new ContentRetrievalException(e.getCause());
        }

        if (successResult.get() != null)
        {
            return successResult.get();
        }
        else
        {
            throw exceptionRef.get();
        }
    }

    private Future<String> makeCall(ApplicationLink link, final String remoteUsername,
            final String url,
            Map<String, String> parameters, Map<String, String> headers,
            final HttpContentHandler handler)
    {
        String urlWithParams = new UriBuilder(Uri.parse(url)).addQueryParameters(parameters).toString();
        final HttpGet httpget = new HttpGet(urlWithParams);
        for (Map.Entry<String,String> entry : headers.entrySet())
        {
            httpget.setHeader(entry.getKey(), entry.getValue());
        }

        HttpContext localContext = new BasicHttpContext();
        oAuthLinkManager.sign(httpget, link, url,
                Maps.transformValues(parameters, new Function<String, List<String>>()
                {
                    @Override
                    public List<String> apply(String from)
                    {
                        return singletonList(from);
                    }
                }));


        log.info("Retrieving content from remote app '{}' on URL '{}' for user '{}'", new Object[]{link.getId().get(), url, remoteUsername});
        FutureCallback<HttpResponse> futureCallback = new FutureCallback<HttpResponse>()
        {
            @Override
            public void completed(HttpResponse result)
            {
                requestKiller.completedRequest(httpget);
                if (result.getStatusLine().getStatusCode() == 200)
                {
                    try
                    {
                        String content = EntityUtils.toString(result.getEntity());
                        handler.onSuccess(content);
                    }
                    catch (IOException e)
                    {
                        log.warn("Unable to retrieve information from '{}' as user '{}' due to: {}",
                                new Object[]{url, remoteUsername, e.getMessage()});
                        handler.onError(new ContentRetrievalException(
                                result.getStatusLine().getReasonPhrase()));
                    }
                }
                else
                {
                    log.warn(
                            "Unable to retrieve information from '{}' as user '{}' due to status " +
                                    "{}",
                            new Object[]{url, remoteUsername,
                                    result.getStatusLine().getStatusCode()});
                    handler.onError(new ContentRetrievalException(
                            result.getStatusLine().getReasonPhrase()));
                }
            }

            @Override
            public void failed(Exception ex)
            {
                requestKiller.completedRequest(httpget);
                log.warn("Unable to retrieve information from '{}' as user '{}' due to: {}",
                        new Object[]{url, remoteUsername, ex.getMessage()});
                handler.onError(new ContentRetrievalException(ex));
            }

            @Override
            public void cancelled()
            {
                requestKiller.completedRequest(httpget);
                log.debug("Request {} cancelled", url);
                handler.onError(
                        new RetrievalTimeoutException("Timeout waiting for " + url));
            }
        };
        requestKiller.registerRequest(new NotifyingAbortableHttpRequest(httpget, futureCallback), 10);
        final Future<HttpResponse> futureResponse = httpClient.execute(httpget, localContext, futureCallback);
        return new ResponseToStringFuture(futureResponse);
    }

    @Override
    public void destroy() throws Exception
    {
        httpClient.getConnectionManager().shutdown();
    }

    @Override
    public void postIgnoreResponse(ApplicationLink link, final String url, String jsonBody)
    {
        final String user = userManager.getRemoteUsername();
        final HttpPost httpPost = new HttpPost(url);
        oAuthLinkManager.sign(httpPost, link, url, Collections.<String, List<String>>emptyMap());
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        try
        {
            httpPost.setEntity(new StringEntity(jsonBody));
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
        log.info("Posting information to '{}' as user '{}'", url, user);
        requestKiller.registerRequest(httpPost, 30);
        httpClient.execute(httpPost, new FutureCallback<HttpResponse>()
        {
            @Override
            public void completed(HttpResponse httpResponse)
            {
                requestKiller.completedRequest(httpPost);
                if (httpResponse.getStatusLine().getStatusCode() != 200)
                {
                    log.error("Unable to post to " + url + " due to " + httpResponse.getStatusLine().toString());
                }
                else
                {
                    log.debug("Posting information to '{}' as user '{}' successful", url, user);
                }
            }

            @Override
            public void failed(Exception e)
            {
                requestKiller.completedRequest(httpPost);
                log.warn("Unable to post information to '{}' as user '{}' due to: {}", new Object
                        []{url, user, e.getMessage()});
                if (log.isDebugEnabled())
                {
                    log.debug("Exception", e);
                }
            }

            @Override
            public void cancelled()
            {
                requestKiller.completedRequest(httpPost);
                log.debug("Posting information to '{}' as user '{}' cancelled", url, user);
            }
        });
    }

    /**
     * This is a huge hack because the httpclient async lib doesn't seem to support aborting
     * requests
     */
    private class NotifyingAbortableHttpRequest implements AbortableHttpRequest
    {

        private final AbortableHttpRequest delegate;
        private final FutureCallback<HttpResponse> callback;
        private NotifyingAbortableHttpRequest(AbortableHttpRequest delegate,
                FutureCallback<HttpResponse> callback)
        {
            this.delegate = delegate;
            this.callback = callback;
        }

        @Override
        public void setConnectionRequest(ClientConnectionRequest connRequest) throws IOException
        {
            delegate.setConnectionRequest(connRequest);
        }

        @Override
        public void setReleaseTrigger(ConnectionReleaseTrigger releaseTrigger) throws IOException
        {
            delegate.setReleaseTrigger(releaseTrigger);
        }

        @Override
        public void abort()
        {
            delegate.abort();
            // workaround as this doesn't seem to be getting called during an abort.  In fact,
            // the request doesn't seem to be killed at all.  Note, this means the remote server
            // is perodically sending back data enough to evade the socket timeout and has
            // instead triggered the request killer.
            callback.cancelled();
        }

    }
    private static class ResponseToStringFuture implements Future<String>
    {
        private final Future<HttpResponse> futureResponse;

        public ResponseToStringFuture(Future<HttpResponse> futureResponse)
        {
            this.futureResponse = futureResponse;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning)
        {
            return futureResponse.cancel(mayInterruptIfRunning);
        }

        @Override
        public boolean isCancelled()
        {
            return futureResponse.isCancelled();
        }

        @Override
        public boolean isDone()
        {
            return futureResponse.isDone();
        }

        @Override
        public String get() throws InterruptedException, ExecutionException
        {
            return responseToString(futureResponse.get());
        }

        @Override
        public String get(long timeout, TimeUnit unit) throws InterruptedException,
                ExecutionException,
                TimeoutException
        {
            return responseToString(futureResponse.get(timeout, unit));
        }

        String responseToString(HttpResponse response)
        {
            try
            {
                return EntityUtils.toString(response.getEntity());
            }
            catch (IOException e)
            {
                throw new ContentRetrievalException(e);
            }
        }
    }
}
