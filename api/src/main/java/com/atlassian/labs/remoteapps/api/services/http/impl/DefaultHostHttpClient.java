package com.atlassian.labs.remoteapps.api.services.http.impl;

import com.atlassian.labs.remoteapps.api.services.RequestContext;
import com.atlassian.labs.remoteapps.api.services.SignedRequestHandler;
import com.atlassian.labs.remoteapps.api.services.http.AsyncHttpClient;
import com.atlassian.labs.remoteapps.api.services.http.HostHttpClient;
import com.atlassian.labs.remoteapps.api.services.http.Request;
import com.atlassian.labs.remoteapps.api.services.http.Response;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

public class DefaultHostHttpClient implements HostHttpClient
{
    private AsyncHttpClient asyncHttpClient;
    private RequestContext requestContext;
    private SignedRequestHandler signedRequestHandler;
    private final Logger log = LoggerFactory.getLogger(DefaultAsyncHttpClient.class);

    public DefaultHostHttpClient(AsyncHttpClient asyncHttpClient,
                                 RequestContext requestContext,
                                 SignedRequestHandler signedRequestHandler)
    {
        this.asyncHttpClient = asyncHttpClient;
        this.requestContext = requestContext;
        this.signedRequestHandler = signedRequestHandler;
    }

    @Override
    public ListenableFuture<Response> get(String uri)
    {
        return get(new Request(uri));
    }

    @Override
    public ListenableFuture<Response> get(String uri, FutureCallback<Response> callback)
    {
        return get(new Request(uri), callback);
    }

    @Override
    public ListenableFuture<Response> get(Request request)
    {
        request.setMethod(Request.Method.GET);
        return request(request);
    }

    @Override
    public ListenableFuture<Response> get(Request request, FutureCallback<Response> callback)
    {
        ListenableFuture<Response> future = get(request);
        Futures.addCallback(future, callback);
        return future;
    }

    @Override
    public ListenableFuture<Response> post(String uri, String contentType, String entity)
    {
        return post(new Request(uri, contentType, entity));
    }

    @Override
    public ListenableFuture<Response> post(String uri, String contentType, String entity, FutureCallback<Response> callback)
    {
        return post(new Request(uri, contentType, entity), callback);
    }

    @Override
    public ListenableFuture<Response> post(Request request)
    {
        request.setMethod(Request.Method.POST);
        return request(request);
    }

    @Override
    public ListenableFuture<Response> post(Request request, FutureCallback<Response> callback)
    {
        ListenableFuture<Response> future = post(request);
        Futures.addCallback(future, callback);
        return future;
    }

    @Override
    public ListenableFuture<Response> put(String uri, String contentType, String entity)
    {
        return put(new Request(uri, contentType, entity));
    }

    @Override
    public ListenableFuture<Response> put(String uri, String contentType, String entity, FutureCallback<Response> callback)
    {
        return put(new Request(uri, contentType, entity), callback);
    }

    @Override
    public ListenableFuture<Response> put(Request request)
    {
        request.setMethod(Request.Method.PUT);
        return request(request);
    }

    @Override
    public ListenableFuture<Response> put(Request request, FutureCallback<Response> callback)
    {
        ListenableFuture<Response> future = put(request);
        Futures.addCallback(future, callback);
        return future;
    }

    @Override
    public ListenableFuture<Response> delete(String uri)
    {
        return delete(new Request(uri));
    }

    @Override
    public ListenableFuture<Response> delete(String uri, FutureCallback<Response> callback)
    {
        return delete(new Request(uri), callback);
    }

    @Override
    public ListenableFuture<Response> delete(Request request)
    {
        request.setMethod(Request.Method.DELETE);
        return request(request);
    }

    @Override
    public ListenableFuture<Response> delete(Request request, FutureCallback<Response> callback)
    {
        ListenableFuture<Response> future = delete(request);
        Futures.addCallback(future, callback);
        return future;
    }

    @Override
    public ListenableFuture<Response> request(Request request)
    {
        // validate the high-level request state
        request.validate();

        // get the current oauth client key and die if it's not available
        String clientKey = requestContext.getClientKey();
        if (clientKey == null)
        {
            throw new IllegalStateException("Unable to execute host http request without base url");
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

        String uriStr = uriBuf.toString();

        // get the request headers
        Map<String, String> headers = request.getHeaders();

        // sign the request
        String method = request.getMethod().toString();
        String authHeader = signedRequestHandler.getAuthorizationHeaderValue(origUriStr, method, userId);
        headers.put("Authorization", authHeader);

        // log the outgoing request
        // @todo response logging
        // @todo move this down into async http client?
        if (log.isDebugEnabled())
        {
            StringBuilder buf = new StringBuilder();
            String lf = System.getProperty("line.separator");
            buf.append("HTTP/1.1 ").append(method).append(" ").append(uriStr).append(lf);
            for (Map.Entry<String, String> header : headers.entrySet())
            {
                buf.append(header.getKey()).append(": ").append(header.getValue()).append(lf);
            }
            buf.append(lf);
            if (request.hasEntity())
            {
                try
                {
                    // if entity is a stream, this could cause a big perf hit, so only do it when debug is on
                    String entity = request.getEntity();
                    buf.append(entity).append(lf);
                    // re-set the entity to reset the one-time-read state in prep for the real read below
                    request.setEntity(entity);
                }
                catch (IOException e)
                {
                    buf.append("(failed to read request entity)").append(lf).append(e.toString());
                }
            }
            log.debug(buf.toString());
        }

        // execute the request via the async request service
        ListenableFuture<HttpResponse> fromFuture
            = asyncHttpClient.request(method, uriStr, headers, request.getEntityStream());

        // translate the low-level response future to that of this higher-level api
        final SettableFuture<Response> toFuture = SettableFuture.create();
        Futures.addCallback(fromFuture, new FutureCallback<HttpResponse>()
        {
            @Override
            public void onSuccess(HttpResponse result)
            {
                try
                {
                    toFuture.set(translate(result));
                }
                catch (IOException e)
                {
                    onFailure(e);
                }
            }

            @Override
            public void onFailure(Throwable t)
            {
                toFuture.setException(t);
            }
        });
        return toFuture;
    }

    @Override
    public ListenableFuture<Response> request(Request request, FutureCallback<Response> callback)
    {
        ListenableFuture<Response> future = request(request);
        Futures.addCallback(future, callback);
        return future;
    }

    private Response translate(HttpResponse httpResponse)
        throws IOException
    {
        StatusLine status = httpResponse.getStatusLine();
        Response response = new Response();
        response.setStatusCode(status.getStatusCode());
        response.setStatusText(status.getReasonPhrase());
        Header[] httpHeaders = httpResponse.getAllHeaders();
        for (Header httpHeader : httpHeaders)
        {
            response.setHeader(httpHeader.getName(), httpHeader.getValue());
        }
        HttpEntity entity = httpResponse.getEntity();
        if (entity != null)
        {
            response.setEntityStream(entity.getContent());
        }
        return response;
    }
}
