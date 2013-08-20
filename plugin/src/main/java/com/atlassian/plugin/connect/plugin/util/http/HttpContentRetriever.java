package com.atlassian.plugin.connect.plugin.util.http;

import java.net.URI;
import java.util.Map;
import java.util.regex.Pattern;

import com.atlassian.plugin.connect.spi.http.AuthorizationGenerator;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.util.concurrent.Promise;

/**
 * Retrieves and caches http content.
 */
public interface HttpContentRetriever
{
    void flushCacheByUriPattern(Pattern urlPattern);

    /**
     * Retrieves HTTP content asynchronously using the given parameters. Parameters will be added as query parameters for
     * {@code GET}, {@code DELETE} and {@code HEAD}, and as {@code application/x-www-form-urlencoded} parameters in the
     * body for {@code POST}, {@code PUT} and {@code TRACE}.
     * It is up to implementation to limit the list of accepted HTTP methods.
     *
     * @param authorizationGenerator the generator for the authorisation header
     * @param method the HTTP method to use
     * @param url the url to hit
     * @param parameters the parameters to use.
     * @param headers the headers
     * @param pluginKey the key of the plugin to retrieve the content as
     * @return a promise of the retrieved content
     * @since 0.10
     */
    public Promise<String> async(AuthorizationGenerator authorizationGenerator,
                                 HttpMethod method,
                                 URI url,
                                 Map<String, String> parameters,
                                 Map<String, String> headers,
                                 String pluginKey);


    /**
     * Retrieves HTTP content asynchronously using GET and the given parameters
     *
     * @param authorizationGenerator the generator for the authorisation header
     * @param remoteUsername the user to retrieve the content as
     * @param url the url to hit
     * @param parameters the parameters to use
     * @param headers the headers
     * @param pluginKey the key of the plugin to retrieve the content as
     * @return a promise of the retrieved content
     * @deprecated since 0.10 use {@link #async(AuthorizationGenerator, HttpMethod, java.net.URI, java.util.Map, java.util.Map, String)} instead
     */
    @Deprecated
    Promise<String> getAsync(AuthorizationGenerator authorizationGenerator,
                             String remoteUsername,
                             URI url,
                             Map<String, String> parameters,
                             Map<String, String> headers,
                             String pluginKey);
}
