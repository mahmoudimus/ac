package com.atlassian.plugin.remotable.plugin.util.http;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.remotable.host.common.service.http.HttpRequestCompletedEvent;
import com.atlassian.plugin.remotable.host.common.service.http.HttpRequestFailedEvent;
import com.atlassian.plugin.remotable.host.common.service.http.RequestKiller;
import com.atlassian.plugin.remotable.plugin.ContentRetrievalException;
import com.atlassian.plugin.remotable.plugin.RetrievalTimeoutException;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.uri.Uri;
import com.atlassian.uri.UriBuilder;
import com.atlassian.util.concurrent.ThreadFactories;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.cache.HeaderConstants;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpAsyncClient;
import org.apache.http.impl.nio.client.DefaultHttpAsyncClient;
import org.apache.http.impl.nio.conn.AsyncSchemeRegistryFactory;
import org.apache.http.impl.nio.conn.PoolingClientAsyncConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
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
import java.net.ProxySelector;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

import static java.util.Collections.singletonList;

/**
 * Retrieves http content asynchronously and caches its contents in memory according to the returned headers
 */
public class CachingHttpContentRetriever implements DisposableBean, HttpContentRetriever
{
    private final FlushableHttpCacheStorage httpCacheStorage;
    CachingHttpAsyncClient httpClient;
    private final Logger log = LoggerFactory.getLogger(CachingHttpContentRetriever.class);
    private final RequestKiller requestKiller;
    private EventPublisher eventPublisher;

    public CachingHttpContentRetriever(PluginRetrievalService pluginRetrievalService,
                                       RequestKiller requestKiller, EventPublisher eventPublisher)
    {
        this.requestKiller = requestKiller;
        this.eventPublisher = eventPublisher;
        CacheConfig cacheConfig = new CacheConfig();
        cacheConfig.setMaxCacheEntries(1000);
        cacheConfig.setSharedCache(false);
        cacheConfig.setMaxObjectSize(100 * 1024L);

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
            final PoolingClientAsyncConnectionManager connmgr = new PoolingClientAsyncConnectionManager(reactor,
                    AsyncSchemeRegistryFactory.createDefault(), 3, TimeUnit.SECONDS)
            {
                @Override
                protected void finalize() throws Throwable
                {
                    // prevent the PoolingClientAsyncConnectionManager from logging - this causes exceptions due to
                    // the ClassLoader probably having been removed when the plugin shuts down.  Added a
                    // PluginEventListener to make sure the shutdown method is called while the plugin classloader
                    // is still active.
                }
            };

            // allow a high-ish number of outgoing connections as it should be fine given our nio backend, otherwise
            // a few slow requests could cause all subsequent requests to timeout
            connmgr.setDefaultMaxPerRoute(100);
            client = new DefaultHttpAsyncClient(connmgr);
        }
        catch (IOReactorException e)
        {
            throw new RuntimeException("Reactor not set up correctly", e);
        }

        HttpParams params = client.getParams();
        HttpProtocolParams.setUserAgent(params, "Atlassian-Remotable-Plugins/" + pluginRetrievalService.getPlugin().getPluginInformation().getVersion());

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
    public Future<String> getAsync(final AuthorizationGenerator authorizationGenerator, final String remoteUsername,
            final URI url,
            final Map<String, String> parameters, final Map<String, String> headers,
            final HttpContentHandler handler, final String pluginKey)
    {
        final long start = System.currentTimeMillis();
        final String urlWithParams = new UriBuilder(Uri.fromJavaUri(url)).addQueryParameters(parameters).toString();
        final HttpGet httpget = new HttpGet(urlWithParams);
        for (Map.Entry<String,String> entry : headers.entrySet())
        {
            httpget.setHeader(entry.getKey(), entry.getValue());
        }

        HttpContext localContext = new BasicHttpContext();
        httpget.addHeader(HttpHeaders.AUTHORIZATION, authorizationGenerator.generate(
            httpget.getMethod(), url, Maps.transformValues(parameters, new Function<String, List<String>>()
        {
            @Override
            public List<String> apply(String from)
            {
                return singletonList(from);
            }
        })));

        final Map<String, String> properties = Maps.newHashMap();
        properties.put("purpose", "content-retrieval");
        properties.put("moduleKey", pluginKey);

        log.info("Retrieving content from '{}' for user '{}'", new Object[]{url, remoteUsername});
        FutureCallback<HttpResponse> futureCallback = new FutureCallback<HttpResponse>()
        {
            @Override
            public void completed(HttpResponse result)
            {
                long elapsed = System.currentTimeMillis() - start;
                requestKiller.completedRequest(httpget);
                int statusCode = result.getStatusLine().getStatusCode();
                if (statusCode == 200)
                {
                    if (log.isDebugEnabled())
                    {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Response protocol: " + result.getProtocolVersion());
                        sb.append("Response headers: ");
                        for (Header header : result.getAllHeaders())
                        {
                            sb.append("\n\t").append(header.getName()).append(": ").append(header.getValue());
                        }
                        sb.append("Has uri: " + urlWithParams.contains("?"));
                        sb.append("Has expires: " + (result.getFirstHeader(HeaderConstants.EXPIRES) != null));
                        log.debug("Request for " + urlWithParams + ":\n");
                    }
                    try
                    {
                        String content = EntityUtils.toString(result.getEntity());
                        eventPublisher.publish(new HttpRequestCompletedEvent(urlWithParams, statusCode, elapsed, properties));
                        handler.onSuccess(content);
                    }
                    catch (IOException e)
                    {
                        eventPublisher.publish(new HttpRequestFailedEvent(urlWithParams, statusCode, elapsed, properties));
                        log.warn("Unable to retrieve information from '{}' as user '{}' due to: {}",
                            new Object[]{url, remoteUsername, e.toString()});
                        log.debug("Error retrieving remote information", e);
                        handler.onError(new ContentRetrievalException(
                            result.getStatusLine().getReasonPhrase()));
                    }
                }
                else
                {
                    eventPublisher.publish(new HttpRequestFailedEvent(urlWithParams, statusCode, elapsed, properties));
                    log.warn(
                        "Unable to retrieve information from '{}' as user '{}' due to status " +
                            "{}",
                        new Object[]{url, remoteUsername,
                            result.getStatusLine().getStatusCode()});
                    handler.onError(new ContentRetrievalException(
                        result.getStatusLine().getReasonPhrase()));
                }
            }

            private void handleTimeout(long elapsed)
            {
                String error = "Timeout retrieving data from plugin '" + pluginKey + "'";
                eventPublisher.publish(
                        new HttpRequestFailedEvent(urlWithParams, error, elapsed, properties));
                log.warn("Timeout retrieving information from '{}' as user '{}'",
                        new Object[]{url, remoteUsername});
                handler.onError(new RetrievalTimeoutException(error));
            }

            @Override
            public void failed(Exception ex)
            {
                long elapsed = System.currentTimeMillis() - start;
                requestKiller.completedRequest(httpget);
                if (ex instanceof SocketTimeoutException)
                {
                    handleTimeout(elapsed);
                }
                else if (ex instanceof TimeoutException)
                {
                    eventPublisher.publish(new HttpRequestFailedEvent(urlWithParams, 0, elapsed, properties));
                    log.warn("Unable to retrieve information from '{}' as user '{}' due to the max connections already " +
                         "in use", url, remoteUsername);
                    handler.onError(new ContentRetrievalException("Max outgoing connections already in use"));
                }
                else
                {
                    eventPublisher.publish(new HttpRequestFailedEvent(urlWithParams, 0, elapsed, properties));
                    log.warn("Unable to retrieve information from '{}' as user '{}' due to: {}",
                            new Object[]{url, remoteUsername, ex.toString()});
                    handler.onError(new ContentRetrievalException(ex));
                }
            }

            @Override
            public void cancelled()
            {
                long elapsed = System.currentTimeMillis() - start;
                requestKiller.completedRequest(httpget);
                log.debug("Request {} cancelled", url);
                handleTimeout(elapsed);
            }
        };
        requestKiller.registerRequest(httpget, 10);
        final Future<HttpResponse> futureResponse = httpClient.execute(httpget, localContext, futureCallback);

        if (futureResponse.isDone())
        {
            log.debug("Request {} retrieved from the cache", url);
        }
        else
        {
            log.debug("Request {} isn't in the cache, retrieving...", url);
        }
        return new ResponseToStringFuture(futureResponse);
    }

    @Override
    public void destroy() throws Exception
    {
        httpClient.getConnectionManager().shutdown();
    }

    static class ResponseToStringFuture implements Future<String>
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

        static String responseToString(HttpResponse response)
        {
            try
            {
                return LimitedEntityUtils.toString(response.getEntity());
            }
            catch (IOException e)
            {
                throw new ContentRetrievalException(e);
            }
        }
    }

}
