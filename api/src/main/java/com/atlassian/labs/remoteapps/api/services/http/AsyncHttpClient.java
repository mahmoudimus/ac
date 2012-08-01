package com.atlassian.labs.remoteapps.api.services.http;

import com.google.common.util.concurrent.ListenableFuture;
import org.apache.http.HttpResponse;

import java.io.InputStream;
import java.util.Map;

public interface AsyncHttpClient
{
    public enum HttpMethod { GET, POST, PUT, DELETE }

    ListenableFuture<HttpResponse> get(String uri, Map<String, String> headers);

    ListenableFuture<HttpResponse> post(String uri, Map<String, String> headers, InputStream entity);

    ListenableFuture<HttpResponse> put(String uri, Map<String, String> headers, InputStream entity);

    ListenableFuture<HttpResponse> delete(String uri, Map<String, String> headers);

    ListenableFuture<HttpResponse> request(String method, String uri, Map<String, String> headers, InputStream entity);

    ListenableFuture<HttpResponse> request(HttpMethod method, String uri, Map<String, String> headers, InputStream entity);
}
