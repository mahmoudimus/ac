package com.atlassian.labs.remoteapps.host.common.service.http;

import com.atlassian.labs.remoteapps.api.service.SignedRequestHandler;
import com.atlassian.labs.remoteapps.api.service.http.HostHttpClient;
import com.atlassian.labs.remoteapps.api.service.http.ResponsePromise;
import com.atlassian.labs.remoteapps.host.common.service.DefaultRequestContext;

import java.io.UnsupportedEncodingException;
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
        if (request.getUri().matches("^[\\w]+:.*"))
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

        request.setUri(uriBuf.toString());

        String method = request.getMethod().toString();
        String authHeader = signedRequestHandler.getAuthorizationHeaderValue(origUriStr, method, userId);
        request
            .setHeader("Authorization", authHeader)
            // capture request properties for analytics
            .setAttribute("purpose", "host-request")
            .setAttribute("clientKey", clientKey);

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
}
