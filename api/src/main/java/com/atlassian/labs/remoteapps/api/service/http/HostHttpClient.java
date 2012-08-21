package com.atlassian.labs.remoteapps.api.service.http;

import com.atlassian.labs.remoteapps.api.Promise;

/**
 * Used to make requests back to the host application.  Implementations handle
 * oauth signing and user propagation.  URIs should be relative to the host app url,
 * including the context path (e.g. relative to something like http://localhost:2990/jira).
 */
public interface HostHttpClient
{
    Promise<Response> get(String uri);

    Promise<Response> get(Request request);

    Promise<Response> post(String uri, String contentType, String entity);

    Promise<Response> post(Request request);

    Promise<Response> put(String uri, String contentType, String entity);

    Promise<Response> put(Request request);

    Promise<Response> delete(String uri);

    Promise<Response> delete(Request request);

    Promise<Response> request(Request request);

}