package com.atlassian.labs.remoteapps.api;

/**
 * Callback for promise events
 * // TODO move into the com.atlassian.labs.remoteapps.concurrent package when extracted out of the Remote Apps Plugin project
 */
public interface PromiseCallback<V>
{
    void handle(V value);
}
