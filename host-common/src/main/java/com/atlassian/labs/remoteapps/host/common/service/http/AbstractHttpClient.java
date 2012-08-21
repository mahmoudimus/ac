package com.atlassian.labs.remoteapps.host.common.service.http;

import com.atlassian.labs.remoteapps.api.Promise;
import com.atlassian.labs.remoteapps.api.service.http.HttpClient;
import org.apache.http.HttpResponse;

import java.io.InputStream;
import java.util.Map;

public abstract class AbstractHttpClient implements HttpClient
{
    @Override
    public Promise<HttpResponse> get(String uri, Map<String, String> headers, Map<String, String> properties)
    {
        return request(Method.GET, uri, headers, null, properties);
    }

    @Override
    public Promise<HttpResponse> post(String uri, Map<String, String> headers, InputStream entity, Map<String, String> properties)
    {
        return request(Method.POST, uri, headers, entity, properties);
    }

    @Override
    public Promise<HttpResponse> put(String uri, Map<String, String> headers, InputStream entity, Map<String, String> properties)
    {
        return request(Method.PUT, uri, headers, entity, properties);
    }

    @Override
    public Promise<HttpResponse> delete(String uri, Map<String, String> headers, Map<String, String> properties)
    {
        return request(Method.DELETE, uri, headers, null, properties);
    }

    @Override
    public Promise<HttpResponse> request(String name, String uri, Map<String, String> headers, InputStream entity, Map<String, String> properties)
    {
        return request(Method.valueOf(name), uri, headers, entity, properties);
    }
}
