package com.atlassian.plugin.remotable.plugin.util.http;

/**
 * Callback when http content is retrieved
 */
public interface HttpContentHandler
{
    void onSuccess(String content);
    void onError(ContentRetrievalException ex);
}
