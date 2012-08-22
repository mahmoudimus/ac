package com.atlassian.labs.remoteapps.host.common.service.http;

import com.atlassian.labs.remoteapps.api.service.http.HttpClient;
import com.atlassian.labs.remoteapps.api.service.http.Request;
import com.atlassian.labs.remoteapps.api.service.http.Request.Method;
import com.atlassian.labs.remoteapps.api.service.http.ResponsePromise;

public abstract class AbstractHttpClient implements HttpClient
{
    @Override
    public ResponsePromise get(String uri)
    {
        return get(new Request(uri));
    }

    @Override
    public ResponsePromise get(Request request)
    {
        return request(request.setMethod(Method.GET));
    }

    @Override
    public ResponsePromise post(String uri, String contentType, String entity)
    {
        return post(new Request(uri, contentType, entity));
    }

    @Override
    public ResponsePromise post(Request request)
    {
        return request(request.setMethod(Method.POST));
    }

    @Override
    public ResponsePromise put(String uri, String contentType, String entity)
    {
        return put(new Request(uri, contentType, entity));
    }

    @Override
    public ResponsePromise put(Request request)
    {
        return request(request.setMethod(Method.PUT));
    }

    @Override
    public ResponsePromise delete(String uri)
    {
        return delete(new Request(uri));
    }

    @Override
    public ResponsePromise delete(Request request)
    {
        return request(request.setMethod(Method.DELETE));
    }
}
