package com.atlassian.labs.remoteapps.plugin.util.http;

import com.atlassian.labs.remoteapps.plugin.ContentRetrievalException;

/**
 * Callback when http content is retrieved
 */
public interface HttpContentHandler
{
    void onSuccess(String content);
    void onError(ContentRetrievalException ex);
}
