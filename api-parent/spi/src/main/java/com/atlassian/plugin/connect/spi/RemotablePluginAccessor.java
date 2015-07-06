package com.atlassian.plugin.connect.spi;

import com.atlassian.plugin.connect.spi.http.AuthorizationGenerator;
import com.atlassian.plugin.connect.api.http.HttpMethod;
import com.atlassian.util.concurrent.Promise;

import java.net.URI;
import java.util.Map;

/**
 * Abstracts interactions with the remote plugin
 */
public interface RemotablePluginAccessor
{
    String getKey();

    /**
     * The start of the URL for HTTP calls to this plugin (e.g. "http://server:1234/contextPath").
     *
     * @return {@link URI} URL prefix for HTTP calls
     */
    URI getBaseUrl();

    URI getTargetUrl(URI targetPath);

    String signGetUrl(URI targetPath, Map<String, String[]> params);

    String createGetUrl(URI targetPath, Map<String, String[]> params);

    Promise<String> executeAsync(HttpMethod method, URI path, Map<String, String[]> params, Map<String, String> headers);

    AuthorizationGenerator getAuthorizationGenerator();

    String getName();
}
