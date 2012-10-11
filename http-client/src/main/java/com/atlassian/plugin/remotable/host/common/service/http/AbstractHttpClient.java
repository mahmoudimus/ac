package com.atlassian.plugin.remotable.host.common.service.http;

import com.atlassian.plugin.remotable.api.service.http.HttpClient;
import com.atlassian.plugin.remotable.api.service.http.Request;
import com.atlassian.plugin.remotable.api.service.http.ResponsePromise;

import java.net.URI;

public abstract class AbstractHttpClient implements HttpClient
{
    @Override
    public Request newRequest()
    {
        return new DefaultRequest(this);
    }

    @Override
    public Request newRequest(URI uri)
    {
        return new DefaultRequest(this, uri);
    }

    @Override
    public Request newRequest(URI uri, String contentType, String entity)
    {
        return new DefaultRequest(this, uri, contentType, entity);
    }

    @Override
    public Request newRequest(String uri)
    {
        return newRequest(URI.create(uri));
    }

    @Override
    public Request newRequest(String uri, String contentType, String entity)
    {
        return newRequest(URI.create(uri), contentType, entity);
    }

    protected abstract ResponsePromise execute(DefaultRequest request);
}
