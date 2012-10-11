package com.atlassian.plugin.remotable.host.common.service.http;

import com.atlassian.plugin.remotable.api.service.SignedRequestHandler;
import com.atlassian.plugin.remotable.api.service.http.HostHttpClient;
import com.atlassian.plugin.remotable.api.service.http.Response;
import com.atlassian.plugin.remotable.api.service.http.ResponsePromise;
import com.atlassian.plugin.remotable.host.common.service.DefaultRequestContext;
import com.google.common.util.concurrent.SettableFuture;

import javax.annotation.Nullable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.concurrent.Callable;

public class DefaultHostHttpClient extends AbstractHttpClient implements HostHttpClient
{
    private DefaultHttpClient httpClient;
    private DefaultRequestContext requestContext;
    private SignedRequestHandler signedRequestHandler;

    public DefaultHostHttpClient(DefaultHttpClient httpClient,
                                 DefaultRequestContext requestContext,
                                 SignedRequestHandler signedRequestHandler)
    {
        this.httpClient = httpClient;
        this.requestContext = requestContext;
        this.signedRequestHandler = signedRequestHandler;
    }

    @Override
    protected ResponsePromise execute(DefaultRequest request)
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
        String baseUrl = signedRequestHandler.getHostBaseUrl(clientKey);

        // build initial absolute request url from the base and the request uri
        String origUriStr = baseUrl + request.getUri();
        StringBuilder uriBuf = new StringBuilder(origUriStr);

        // append the user id to the uri if available
        String userId = requestContext.getUserId();
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

        String method = request.getMethod().toString();
        String authHeader = signedRequestHandler.getAuthorizationHeaderValue(URI.create(origUriStr), method, userId);
        request
            .setHeader("Authorization", authHeader)
            // capture request properties for analytics
            .setAttribute("purpose", "host-request")
            .setAttribute("clientKey", clientKey);

        request.setSettableFutureHandler(new ResponseSettableFutureHandler(requestContext));

        // execute the request via the http client service
        return httpClient.execute(request);
    }

    @Override
    public <T> T callAs(String clientKey, String userId, Callable<T> callable)
    {
        String oldClientKey = requestContext.getClientKey();
        String oldUserId = requestContext.getUserId();
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

    private static class ResponseSettableFutureHandler implements SettableFutureHandler<Response>
    {
        private final SettableFuture<Response> future = SettableFuture.create();
        private final DefaultRequestContext.RequestCallable<Response, Boolean> setCallable;
        private final DefaultRequestContext.RequestCallable<Throwable, Boolean> setExceptionCallable;

        private ResponseSettableFutureHandler(DefaultRequestContext requestContext)
        {
            setCallable = requestContext.createCallableForCurrentRequest(
                    new DefaultRequestContext.RequestCallable<Response, Boolean>()
                    {
                        @Override
                        public Boolean call(Response contextParameter)
                        {
                            return future.set(contextParameter);
                        }
                    });

            setExceptionCallable = requestContext.createCallableForCurrentRequest(
                    new DefaultRequestContext.RequestCallable<Throwable, Boolean>()
                    {
                        @Override
                        public Boolean call(Throwable contextParameter)
                        {
                            return future.setException(contextParameter);
                        }
                    });
        }

        @Override
        public boolean set(@Nullable Response value)
        {
            return setCallable.call(value);
        }

        @Override
        public boolean setException(Throwable throwable)
        {
            return setExceptionCallable.call(throwable);
        }

        @Override
        public SettableFuture<Response> getFuture()
        {
            return future;
        }
    }
}
