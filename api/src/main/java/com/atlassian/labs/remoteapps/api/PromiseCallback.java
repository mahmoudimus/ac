package com.atlassian.labs.remoteapps.api;

/**
 * Callback for promise events
 */
public interface PromiseCallback<V>
{
    void handle(V value);
}
