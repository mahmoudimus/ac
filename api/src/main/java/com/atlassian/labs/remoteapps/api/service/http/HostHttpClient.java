package com.atlassian.labs.remoteapps.api.service.http;

import java.util.concurrent.Callable;

/**
 * Used to make requests back to the host application.  Implementations handle
 * oauth signing and user propagation.  URIs should be relative to the host app url,
 * including the context path (e.g. relative to something like http://localhost:2990/jira).
 */
public interface HostHttpClient extends HttpClient
{
    <T> T callAs(String clientKey, String userId, Callable<T> callable);
}
