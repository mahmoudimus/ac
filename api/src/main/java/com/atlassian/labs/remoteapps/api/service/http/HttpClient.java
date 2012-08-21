package com.atlassian.labs.remoteapps.api.service.http;

import com.atlassian.labs.remoteapps.api.Promise;
import org.apache.http.HttpResponse;

import java.io.InputStream;
import java.util.Map;

public interface HttpClient
{
    public enum Method { GET, POST, PUT, DELETE }

    Promise<HttpResponse> get(String uri, Map<String, String> headers, Map<String, String> properties);

    Promise<HttpResponse> post(String uri, Map<String, String> headers, InputStream entity, Map<String, String> properties);

    Promise<HttpResponse> put(String uri, Map<String, String> headers, InputStream entity, Map<String, String> properties);

    Promise<HttpResponse> delete(String uri, Map<String, String> headers, Map<String, String> properties);

    Promise<HttpResponse> request(String method, String uri, Map<String, String> headers, InputStream entity, Map<String, String> properties);

    Promise<HttpResponse> request(Method method, String uri, Map<String, String> headers, InputStream entity, Map<String, String> properties);
}
