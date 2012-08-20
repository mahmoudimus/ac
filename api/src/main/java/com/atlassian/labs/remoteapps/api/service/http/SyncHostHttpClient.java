package com.atlassian.labs.remoteapps.api.service.http;

public interface SyncHostHttpClient
{
    Response get(String uri);

    Response get(Request request);

    Response post(String uri, String contentType, String entity);

    Response post(Request request);

    Response put(String uri, String contentType, String entity);

    Response put(Request request);

    Response delete(String uri);

    Response delete(Request request);

    Response request(Request request);
}
