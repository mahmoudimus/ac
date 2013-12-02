package com.atlassian.plugin.connect.plugin.capabilities.testobjects;

import com.atlassian.plugin.connect.spi.RemotablePluginAccessor;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.spi.http.AuthorizationGenerator;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.util.concurrent.Promise;

import java.net.URI;
import java.util.Map;

/**
 * @since 1.0
 */
public class RemotablePluginAccessorFactoryForTests implements RemotablePluginAccessorFactory
{

    @Override
    public RemotablePluginAccessor get(final String pluginKey)
    {
        return new RemotablePluginAccessor() {
            @Override
            public String getKey()
            {
                return pluginKey;
            }

            @Override
            public URI getBaseUrl()
            {
                return URI.create("http://www.example.com");
            }

            @Override
            public String signGetUrl(URI targetPath, Map<String, String[]> params)
            {
                return "";
            }

            @Override
            public String createGetUrl(URI targetPath, Map<String, String[]> params)
            {
                return "";
            }

            @Override
            public Promise<String> executeAsync(HttpMethod method, URI path, Map<String, String> params, Map<String, String> headers)
            {
                return null;
            }

            @Override
            public AuthorizationGenerator getAuthorizationGenerator()
            {
                return null;
            }

            @Override
            public String getName()
            {
                return null;
            }
        };
    }
}
