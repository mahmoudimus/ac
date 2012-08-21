package com.atlassian.labs.remoteapps.api.service.http;

import com.google.common.util.concurrent.ListenableFuture;
import org.apache.http.HttpResponse;

import java.io.InputStream;
import java.util.Map;

public interface AsyncHttpClient
{
    public enum Method { GET, POST, PUT, DELETE }

    ListenableFuture<HttpResponse> get(String uri, Map<String, String> headers, Map<String, String> properties);

    ListenableFuture<HttpResponse> post(String uri, Map<String, String> headers, InputStream entity, Map<String, String> properties);

    ListenableFuture<HttpResponse> put(String uri, Map<String, String> headers, InputStream entity, Map<String, String> properties);

    ListenableFuture<HttpResponse> delete(String uri, Map<String, String> headers, Map<String, String> properties);

    ListenableFuture<HttpResponse> request(String method, String uri, Map<String, String> headers, InputStream entity, Map<String, String> properties);

    ListenableFuture<HttpResponse> request(Method method, String uri, Map<String, String> headers, InputStream entity, Map<String, String> properties);
}
