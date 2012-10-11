package com.atlassian.plugin.remotable.plugin.util.http;

import com.atlassian.plugin.remotable.plugin.ContentRetrievalException;

/**
 * Callback when http content is retrieved
 */
public interface HttpContentHandler
{
    void onSuccess(String content);
    void onError(ContentRetrievalException ex);
}
