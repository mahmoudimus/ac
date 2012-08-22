package com.atlassian.labs.remoteapps.api.service.http;

public interface HttpClient
{
    ResponsePromise get(String uri);

    ResponsePromise get(Request request);

    ResponsePromise post(String uri, String contentType, String entity);

    ResponsePromise post(Request request);

    ResponsePromise put(String uri, String contentType, String entity);

    ResponsePromise put(Request request);

    ResponsePromise delete(String uri);

    ResponsePromise delete(Request request);

    ResponsePromise request(Request request);
}
