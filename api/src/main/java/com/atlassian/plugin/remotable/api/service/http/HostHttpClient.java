package com.atlassian.plugin.remotable.api.service.http;

import com.atlassian.httpclient.api.HttpClient;

import java.util.concurrent.Callable;

/**
 * Used to make requests back to the host application.  Implementations handle
 * oauth signing and user propagation.  URIs should be relative to the host app url,
 * including the context path (e.g. relative to something like http://localhost:2990/jira).
 */
public interface HostHttpClient extends HttpClient
{
    /**
     * Runs the provided callable code in a
     * {@link com.atlassian.plugin.remotable.api.service.RequestContext} with the specified
     * clientKey and userId.
     *
     * @param clientKey The clientKey to call as
     * @param userId The user id to call as
     * @param callable The executable code to call
     * @param <T> The return type of the callable code
     * @return The value returned from the callable code
     */
    <T> T callAs(String clientKey, String userId, Callable<T> callable);
}
