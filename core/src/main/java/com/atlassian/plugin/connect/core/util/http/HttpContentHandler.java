package com.atlassian.plugin.connect.core.util.http;

import com.atlassian.plugin.connect.api.util.http.ContentRetrievalException;

/**
 * Callback when http content is retrieved
 */
public interface HttpContentHandler
{
    void onSuccess(String content);
    void onError(ContentRetrievalException ex);
}
