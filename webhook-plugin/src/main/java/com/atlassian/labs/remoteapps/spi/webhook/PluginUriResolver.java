package com.atlassian.labs.remoteapps.spi.webhook;

import java.net.URI;

public interface PluginUriResolver
{
    /**
     * Gets a fully constructed URI for a relative path defined in the plugin with the given key.
     *
     * @param pluginKey the key of the plugin we're resolving the URI for.
     * @param path the relative path
     * @return an absolute URI to the plugin path.
     */
    URI getUri(String pluginKey, URI path);
}
