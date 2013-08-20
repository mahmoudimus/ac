package com.atlassian.plugin.connect.spi;

import java.net.URI;
import java.util.Map;

import com.atlassian.plugin.connect.spi.http.AuthorizationGenerator;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.util.concurrent.Promise;

/**
 * Abstracts interactions with the remote plugin
 */
public interface RemotablePluginAccessor
{
    String getKey();

    URI getDisplayUrl();

    String signGetUrl(URI targetPath, Map<String, String[]> params);

    String createGetUrl(URI targetPath, Map<String, String[]> params);

    Promise<String> executeAsync(HttpMethod method, URI path, Map<String, String> params, Map<String, String> headers);

    AuthorizationGenerator getAuthorizationGenerator();

    String getName();
}
