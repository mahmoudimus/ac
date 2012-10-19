package com.atlassian.plugin.remotable.host.common.service.http;

import com.atlassian.httpclient.api.ForwardingHttpClient;
import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.Request;
import com.atlassian.httpclient.api.Response;
import com.atlassian.httpclient.api.factory.HttpClientFactory;
import com.atlassian.httpclient.api.factory.HttpClientOptions;
import com.atlassian.httpclient.api.factory.SettableFutureHandler;
import com.atlassian.httpclient.api.factory.SettableFutureHandlerFactory;
import com.atlassian.plugin.remotable.api.service.RequestContext;
import com.atlassian.plugin.remotable.api.service.SignedRequestHandler;
import com.atlassian.plugin.remotable.api.service.http.HostHttpClient;
import com.atlassian.plugin.remotable.host.common.service.DefaultRequestContext;
import com.atlassian.util.concurrent.Effect;
import com.google.common.base.Function;
import com.google.common.util.concurrent.SettableFuture;

import javax.annotation.Nullable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.concurrent.Callable;

import static com.google.common.base.Preconditions.*;

public final class DefaultHostHttpClient extends ForwardingHttpClient implements HostHttpClient
{
    private final HttpClient httpClient;
    private final DefaultRequestContext requestContext;

    public DefaultHostHttpClient(HttpClientFactory httpClientFactory, DefaultRequestContext requestContext, SignedRequestHandler signedRequestHandler)
    {
        this.requestContext = checkNotNull(requestContext);
        this.httpClient = checkNotNull(httpClientFactory).create(getHttpOptions(requestContext, checkNotNull(signedRequestHandler)));
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
        options.setResponseSettableFutureHandlerFactory(new SettableFutureHandlerFactory<Response>()
        {
            @Override
            public SettableFutureHandler<Response> create()
            {
                return new ResponseSettableFutureHandler(requestContext);
            }
        });
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

    private static final class ResponseSettableFutureHandler implements SettableFutureHandler<Response>
    {
        private final SettableFuture<Response> future = SettableFuture.create();
        private final Function<Response, Boolean> setCallable;
        private final Function<Throwable, Boolean> setExceptionCallable;

        private ResponseSettableFutureHandler(DefaultRequestContext requestContext)
        {
            setCallable = requestContext.createFunctionForExecutionWithinCurrentRequest(
                    new Function<Response, Boolean>()
                    {
                        @Override
                        public Boolean apply(Response contextParameter)
                        {
                            return future.set(contextParameter);
                        }
                    });

            setExceptionCallable = requestContext.createFunctionForExecutionWithinCurrentRequest(
                    new Function<Throwable, Boolean>()
                    {
                        @Override
                        public Boolean apply(Throwable contextParameter)
                        {
                            return future.setException(contextParameter);
                        }
                    });
        }

        @Override
        public boolean set(@Nullable Response value)
        {
            return setCallable.apply(value);
        }

        @Override
        public boolean setException(Throwable throwable)
        {
            return setExceptionCallable.apply(throwable);
        }

        @Override
        public SettableFuture<Response> getFuture()
        {
            return future;
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
}
