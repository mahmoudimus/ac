package com.atlassian.plugin.connect.spi.util.http;

import com.atlassian.plugin.connect.spi.http.AuthorizationGenerator;
import com.atlassian.plugin.connect.api.http.HttpMethod;
import com.atlassian.util.concurrent.Promise;

import java.net.URI;
import java.util.Map;
import java.util.regex.Pattern;

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
     * @param method                 the HTTP method to use
     * @param url                    the url to hit
     * @param parameters             the parameters to use.
     * @param headers                the headers
     * @param addOnKey               the key of the add-on from which to retrieve the content
     * @return a promise of the retrieved content
     * @since 0.10
     */
    public Promise<String> async(AuthorizationGenerator authorizationGenerator,
                                 HttpMethod method,
                                 URI url,
                                 Map<String, String[]> parameters,
                                 Map<String, String> headers,
                                 String addOnKey);

}
