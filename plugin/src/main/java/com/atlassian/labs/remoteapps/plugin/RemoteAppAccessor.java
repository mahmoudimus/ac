package com.atlassian.labs.remoteapps.plugin;

import com.atlassian.labs.remoteapps.plugin.util.http.AuthorizationGenerator;
import com.atlassian.labs.remoteapps.plugin.util.http.HttpContentHandler;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Abstracts interactions with the remote app
 */
public interface RemoteAppAccessor
{
    String getKey();
    URI getDisplayUrl();

    String signGetUrl(URI targetPath, Map<String, String[]> params);
    String createGetUrl(URI targetPath, Map<String, String[]> params);
    Future<String> executeAsyncGet(String user, URI path, Map<String, String> params,
            Map<String, String> headers, HttpContentHandler handler) throws ContentRetrievalException;


    AuthorizationGenerator getAuthorizationGenerator();

    String getName();
}
