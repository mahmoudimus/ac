package com.atlassian.plugin.remotable.host.common.service.http;

import com.atlassian.fugue.Option;
import com.atlassian.fugue.Pair;
import com.atlassian.httpclient.api.ForwardingHttpClient;
import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.Request;
import com.atlassian.httpclient.api.factory.HttpClientFactory;
import com.atlassian.httpclient.api.factory.HttpClientOptions;
import com.atlassian.httpclient.spi.ThreadLocalContextManager;
import com.atlassian.plugin.remotable.api.service.RequestContext;
import com.atlassian.plugin.remotable.api.service.SignedRequestHandler;
import com.atlassian.plugin.remotable.api.service.http.HostHttpClient;
import com.atlassian.plugin.remotable.host.common.util.ServicePromise;
import com.atlassian.util.concurrent.Effect;
import com.atlassian.util.concurrent.Promise;
import com.atlassian.util.concurrent.Promises;
import com.google.common.base.Function;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import javax.annotation.Nullable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static com.atlassian.fugue.Option.option;
import static com.atlassian.fugue.Pair.*;
import static com.atlassian.plugin.remotable.host.common.util.ServicePromise.promiseProxy;
import static com.google.common.base.Preconditions.*;

public final class DefaultHostHttpClient extends ForwardingHttpClient implements HostHttpClient
{
    private final HttpClient httpClient;
    private final DefaultRequestContext requestContext;

    public DefaultHostHttpClient(Promise<HttpClientFactory> httpClientFactoryPromise, final DefaultRequestContext requestContext, final SignedRequestHandler signedRequestHandler, final ThreadLocalContextManager threadLocalContextManager)
    {
        this.requestContext = checkNotNull(requestContext);
        this.httpClient = promiseProxy(httpClientFactoryPromise.map(new Function<HttpClientFactory, HttpClient>()
        {
            @Override
            public HttpClient apply(HttpClientFactory httpClientFactory)
            {
                return httpClientFactory.create(getHttpOptions(requestContext, checkNotNull(signedRequestHandler)), new DefaultHostHttpClientThreadLocalContextManager(threadLocalContextManager));
            }
        }), HttpClient.class);
    }

    @Override
    protected HttpClient delegate()
    {
        return httpClient;
    }

    private HttpClientOptions getHttpOptions(final DefaultRequestContext requestContext, SignedRequestHandler signedRequestHandler)
    {
        HttpClientOptions options = new HttpClientOptions();
        options.setThreadPrefix("hostclient");
        options.setRequestPreparer(new HostHttpClientRequestPreparer(requestContext, signedRequestHandler));
        return options;
    }

    @Override
    public <T> T callAs(String clientKey, String userId, Callable<T> callable)
    {
        final String oldClientKey = requestContext.getClientKey();
        final String oldUserId = requestContext.getUserId();
        try
        {
            requestContext.setClientKey(clientKey);
            requestContext.setUserId(userId);
            return callable.call();
        }
        catch (Exception e)
        {
            if (e instanceof RuntimeException)
            {
                throw (RuntimeException) e;
            }
            else
            {
                throw new RuntimeException(e);
            }
        }
        finally
        {
            requestContext.setClientKey(oldClientKey);
            requestContext.setUserId(oldUserId);
        }
    }

    private static final class HostHttpClientRequestPreparer implements Effect<Request>
    {
        private final RequestContext requestContext;
        private final SignedRequestHandler signedRequestHandler;

        private HostHttpClientRequestPreparer(RequestContext requestContext, SignedRequestHandler signedRequestHandler)
        {
            this.requestContext = checkNotNull(requestContext);
            this.signedRequestHandler = checkNotNull(signedRequestHandler);
        }

        @Override
        public void apply(Request request)
        {
            // make sure this is a request for a relative url
            if (request.getUri().toString().matches("^[\\w]+:.*"))
            {
                throw new IllegalStateException("Absolute request URIs are not supported for host requests");
            }

            // get the current oauth client key and die if it's not available
            String clientKey = requestContext.getClientKey();
            if (clientKey == null)
            {
                throw new IllegalStateException("Unable to execute host http request without client key");
            }

            // lookup the host base url from the client key
            final String baseUrl = signedRequestHandler.getHostBaseUrl(clientKey);

            // build initial absolute request url from the base and the request uri
            final String origUriStr = baseUrl + request.getUri();
            final StringBuilder uriBuf = new StringBuilder(origUriStr);

            // append the user id to the uri if available
            final String userId = requestContext.getUserId();
            if (userId != null)
            {
                try
                {
                    uriBuf
                            .append(uriBuf.indexOf("?") > 0 ? '&' : '?')
                            .append("user_id")
                            .append('=')
                            .append(URLEncoder.encode(userId, "UTF-8"));
                }
                catch (UnsupportedEncodingException e)
                {
                    throw new RuntimeException(e);
                }
            }

            request.setUri(URI.create(uriBuf.toString()));

            final Request.Method method = request.getMethod();
            final String authHeader = signedRequestHandler.getAuthorizationHeaderValue(URI.create(origUriStr), method.name(), userId);
            request.setHeader("Authorization", authHeader)
                    // capture request properties for analytics
                    .setAttribute("purpose", "host-request")
                    .setAttribute("clientKey", clientKey);
        }
    }

    private static final class DefaultHostHttpClientThreadLocalContextManager<C> implements ThreadLocalContextManager<Pair<DefaultRequestContext.RequestData, Option<C>>>
    {
        private final ThreadLocalContextManager<C> threadLocalContextManager;

        private DefaultHostHttpClientThreadLocalContextManager(ThreadLocalContextManager<C> threadLocalContextManager)
        {
            this.threadLocalContextManager = checkNotNull(threadLocalContextManager);
        }

        @Override
        public Pair<DefaultRequestContext.RequestData, Option<C>> getThreadLocalContext()
        {
            return pair(DefaultRequestContext.getRequestData(), option(threadLocalContextManager.getThreadLocalContext()));
        }

        @Override
        public void setThreadLocalContext(Pair<DefaultRequestContext.RequestData, Option<C>> context)
        {
            DefaultRequestContext.setRequestData(context.left());
            threadLocalContextManager.setThreadLocalContext(context.right().getOrNull());
        }

        @Override
        public void resetThreadLocalContext()
        {

            DefaultRequestContext.clear();
            threadLocalContextManager.resetThreadLocalContext();
        }
    }
}
