package com.atlassian.labs.remoteapps.api.services.http.impl;

import com.atlassian.labs.remoteapps.api.services.http.AsyncHttpClient;
import com.google.common.util.concurrent.ListenableFuture;
import org.apache.http.HttpResponse;

import java.io.InputStream;
import java.util.Map;

public abstract class AbstractAsyncHttpClient implements AsyncHttpClient
{
    @Override
    public ListenableFuture<HttpResponse> get(String uri, Map<String, String> headers, Map<String, String> properties)
    {
        return request(Method.GET, uri, headers, null, properties);
    }

    @Override
    public ListenableFuture<HttpResponse> post(String uri, Map<String, String> headers, InputStream entity, Map<String, String> properties)
    {
        return request(Method.POST, uri, headers, entity, properties);
    }

    @Override
    public ListenableFuture<HttpResponse> put(String uri, Map<String, String> headers, InputStream entity, Map<String, String> properties)
    {
        return request(Method.PUT, uri, headers, entity, properties);
    }

    @Override
    public ListenableFuture<HttpResponse> delete(String uri, Map<String, String> headers, Map<String, String> properties)
    {
        return request(Method.DELETE, uri, headers, null, properties);
    }

    @Override
    public ListenableFuture<HttpResponse> request(String name, String uri, Map<String, String> headers, InputStream entity, Map<String, String> properties)
    {
        return request(Method.valueOf(name), uri, headers, entity, properties);
    }
}
