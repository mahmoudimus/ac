package com.atlassian.plugin.remotable.plugin;

import com.atlassian.plugin.remotable.plugin.util.http.AuthorizationGenerator;
import com.atlassian.plugin.remotable.plugin.util.http.ContentRetrievalException;
import com.atlassian.util.concurrent.Promise;

import java.net.URI;
import java.util.Map;

/**
 * Abstracts interactions with the remote plugin
 */
public interface RemotablePluginAccessor
{
    String getKey();
    URI getDisplayUrl();

    String signGetUrl(URI targetPath, Map<String, String[]> params);
    String createGetUrl(URI targetPath, Map<String, String[]> params);
    Promise<String> executeAsyncGet(String user, URI path, Map<String, String> params,
            Map<String, String> headers) throws ContentRetrievalException;


    AuthorizationGenerator getAuthorizationGenerator();

    String getName();
}
