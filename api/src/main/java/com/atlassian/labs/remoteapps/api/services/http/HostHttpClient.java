package com.atlassian.labs.remoteapps.api.services.http;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;

public interface HostHttpClient
{
    ListenableFuture<Response> get(String uri);

    ListenableFuture<Response> get(String uri, FutureCallback<Response> callback);

    ListenableFuture<Response> get(Request request);

    ListenableFuture<Response> get(Request request, FutureCallback<Response> callback);

    ListenableFuture<Response> post(String uri, String contentType, String entity);

    ListenableFuture<Response> post(String uri, String contentType, String entity, FutureCallback<Response> callback);

    ListenableFuture<Response> post(Request request);

    ListenableFuture<Response> post(Request request, FutureCallback<Response> callback);

    ListenableFuture<Response> put(String uri, String contentType, String entity);

    ListenableFuture<Response> put(String uri, String contentType, String entity, FutureCallback<Response> callback);

    ListenableFuture<Response> put(Request request);

    ListenableFuture<Response> put(Request request, FutureCallback<Response> callback);

    ListenableFuture<Response> delete(String uri);

    ListenableFuture<Response> delete(String uri, FutureCallback<Response> callback);

    ListenableFuture<Response> delete(Request request);

    ListenableFuture<Response> delete(Request request, FutureCallback<Response> callback);

    ListenableFuture<Response> request(Request request);

    ListenableFuture<Response> request(Request request, FutureCallback<Response> callback);
}
