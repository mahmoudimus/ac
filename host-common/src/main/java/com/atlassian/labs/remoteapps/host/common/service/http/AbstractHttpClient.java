package com.atlassian.labs.remoteapps.host.common.service.http;

import com.atlassian.labs.remoteapps.api.service.http.HttpClient;
import com.atlassian.labs.remoteapps.api.service.http.Request;
import com.atlassian.labs.remoteapps.api.service.http.ResponsePromise;

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

    protected abstract ResponsePromise execute(DefaultRequest request);
}
