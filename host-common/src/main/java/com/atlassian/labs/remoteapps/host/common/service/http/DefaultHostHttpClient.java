package com.atlassian.labs.remoteapps.host.common.service.http;

import com.atlassian.labs.remoteapps.api.Promise;
import com.atlassian.labs.remoteapps.api.service.RequestContext;
import com.atlassian.labs.remoteapps.api.service.SignedRequestHandler;
import com.atlassian.labs.remoteapps.api.service.http.HttpClient;
import com.atlassian.labs.remoteapps.api.service.http.HostHttpClient;
import com.atlassian.labs.remoteapps.api.service.http.Request;
import com.atlassian.labs.remoteapps.api.service.http.Response;
import com.atlassian.labs.remoteapps.host.common.service.DefaultRequestContext;
import com.atlassian.labs.remoteapps.spi.WrappingPromise;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
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
import java.util.concurrent.Callable;

public class DefaultHostHttpClient implements HostHttpClient
{
    private HttpClient httpClient;
    private DefaultRequestContext requestContext;
    private SignedRequestHandler signedRequestHandler;
    private final Logger log = LoggerFactory.getLogger(DefaultHttpClient.class);

    public DefaultHostHttpClient(HttpClient httpClient,
                                 DefaultRequestContext requestContext,
                                 SignedRequestHandler signedRequestHandler)
    {
        this.httpClient = httpClient;
        this.requestContext = requestContext;
        this.signedRequestHandler = signedRequestHandler;
    }

    @Override
    public Promise<Response> get(String uri)
    {
        return get(new Request(uri));
    }

    @Override
    public Promise<Response> get(Request request)
    {
        request.setMethod(Request.Method.GET);
        return request(request);
    }

    @Override
    public Promise<Response> post(String uri, String contentType, String entity)
    {
        return post(new Request(uri, contentType, entity));
    }

    @Override
    public Promise<Response> post(Request request)
    {
        request.setMethod(Request.Method.POST);
        return request(request);
    }

    @Override
    public Promise<Response> put(String uri, String contentType, String entity)
    {
        return put(new Request(uri, contentType, entity));
    }

    @Override
    public Promise<Response> put(Request request)
    {
        request.setMethod(Request.Method.PUT);
        return request(request);
    }

    @Override
    public Promise<Response> delete(String uri)
    {
        return delete(new Request(uri));
    }

    @Override
    public Promise<Response> delete(Request request)
    {
        request.setMethod(Request.Method.DELETE);
        return request(request);
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

    @Override
    public Promise<Response> request(Request request)
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

        // capture request properties for analytics
        Map<String, String> properties = Maps.newHashMap();
        properties.put("purpose", "host-request");
        properties.put("clientKey", clientKey);

        // execute the request via the async request service
        Promise<HttpResponse> fromFuture
            = httpClient.request(method, uriStr, headers, request.getEntityStream(), properties);

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
        return new WrappingPromise<Response>(toFuture);
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
