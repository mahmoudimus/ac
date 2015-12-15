package com.atlassian.plugin.connect.api.request;

import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.regex.Pattern;

import com.atlassian.plugin.connect.api.auth.AuthorizationGenerator;
import com.atlassian.util.concurrent.Promise;

import org.apache.http.entity.ContentType;

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
     * @param headers                the headers to use. 'Content-Type' is ignored, and is set
     *                               to {@link ContentType#APPLICATION_FORM_URLENCODED}.
     *                               Use {@link #async(AuthorizationGenerator, HttpMethod, URI, Map, Map, InputStream, String)}
     *                               if you want to use a different content type.
     * @param addOnKey               the key of the add-on from which to retrieve the content
     * @return a promise of the retrieved content
     * @since 0.10
     */
    Promise<String> async(AuthorizationGenerator authorizationGenerator,
                          HttpMethod method,
                          URI url,
                          Map<String, String[]> parameters,
                          Map<String, String> headers,
                          String addOnKey);

    /**
     * Retrieves HTTP content asynchronously using the given parameters. Parameters will be added as query parameters for
     * {@code GET}, {@code DELETE} and {@code HEAD}, as well as {@code POST}, {@code PUT} and {@code TRACE}.
     * The {@code body} is then added as the entity for {@code POST}, {@code PUT} and {@code TRACE} methods, and ignored for
     * {@code GET}, {@code DELETE} and {@code HEAD}.
     * It is up to implementation to limit the list of accepted HTTP methods.
     *
     * @param authorizationGenerator the generator for the authorisation header
     * @param method                 the HTTP method to use
     * @param url                    the url to hit
     * @param parameters             the parameters to use.
     * @param headers                the headers to use. If 'Content-Type' is null, then defaults to {@link ContentType#APPLICATION_JSON}
     * @param body                   the inputstream which returns the body, if method is POST, or PUT, or TRACE.
     * @param addOnKey               the key of the add-on from which to retrieve the content
     * @return a promise of the retrieved content
     */
    Promise<String> async(AuthorizationGenerator authorizationGenerator,
                          HttpMethod method,
                          URI url,
                          Map<String, String[]> parameters,
                          Map<String, String> headers,
                          InputStream body,
                          String addOnKey);

}
