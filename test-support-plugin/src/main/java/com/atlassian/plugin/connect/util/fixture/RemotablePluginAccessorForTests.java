package com.atlassian.plugin.connect.util.fixture;

import com.atlassian.plugin.connect.api.http.HttpMethod;
import com.atlassian.plugin.connect.api.util.UriBuilderUtils;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessor;
import com.atlassian.plugin.connect.spi.http.AuthorizationGenerator;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.uri.Uri;
import com.atlassian.uri.UriBuilder;
import com.atlassian.util.concurrent.Promise;

import java.net.URI;
import java.util.Map;

public class RemotablePluginAccessorForTests implements RemotablePluginAccessor
{

    private final String pluginKey;
    private final String pluginName;
    private final String baseUrl;

    public RemotablePluginAccessorForTests(String pluginKey, String pluginName, String baseUrl)
    {
        this.pluginKey = pluginKey;
        this.pluginName = pluginName;
        this.baseUrl = baseUrl;
    }

    @Override
    public String getKey()
    {
        return pluginKey;
    }

    @Override
    public String getName()
    {
        return pluginName;
    }

    @Override
    public URI getBaseUrl()
    {
        return URI.create(baseUrl);
    }

    @Override
    public URI getTargetUrl(URI targetPath)
    {
        if (targetPath.isAbsolute())
        {
            throw new IllegalArgumentException("Target url was absolute (" + targetPath.toString() + "). Expected relative path to base URL of add-on (" + getBaseUrl().toString() + ").");
        }

        Uri baseUri = Uri.fromJavaUri(getBaseUrl());
        String path = baseUri.getPath() + "/" + targetPath.getRawPath();
        path = path.replaceAll("/+", "/");

        UriBuilder uriBuilder = new UriBuilder(baseUri);
        uriBuilder.setPath(path);
        uriBuilder.setQuery(targetPath.getRawQuery());
        return uriBuilder.toUri().toJavaUri();
    }

    @Override
    public String signGetUrl(URI targetPath, Map<String, String[]> params)
    {
        return "";
    }

    @Override
    public String createGetUrl(URI targetPath, Map<String, String[]> params)
    {
        UriBuilder uriBuilder = new UriBuilder(Uri.fromJavaUri(getTargetUrl(targetPath)));
        UriBuilderUtils.addQueryParameters(uriBuilder, params);
        return uriBuilder.toString();
    }

    @Override
    public Promise<String> executeAsync(HttpMethod method, URI path, Map<String, String[]> params, Map<String, String> headers, UserProfile remoteUser)
    {
        return null;
    }

    @Override
    public AuthorizationGenerator getAuthorizationGenerator()
    {
        return null;
    }
}
