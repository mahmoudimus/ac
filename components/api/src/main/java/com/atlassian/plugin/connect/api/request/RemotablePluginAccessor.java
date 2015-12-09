package com.atlassian.plugin.connect.api.request;

import java.io.InputStream;
import java.net.URI;
import java.util.Map;

import com.atlassian.plugin.connect.api.auth.AuthorizationGenerator;
import com.atlassian.util.concurrent.Promise;

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

    Promise<String> executeAsync(HttpMethod method, URI path, Map<String, String[]> params, Map<String, String> headers, InputStream body);

    AuthorizationGenerator getAuthorizationGenerator();

    String getName();
}
