package com.atlassian.plugin.connect.api.request;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import com.atlassian.plugin.connect.api.auth.AuthorizationGenerator;
import com.atlassian.util.concurrent.Promise;

import org.apache.http.entity.ContentType;

/**
 * Retrieves and caches http content.
 */
public interface HttpContentRetriever
{
    InputStream EMPTY_STREAM = new ByteArrayInputStream(new byte[0]);

    void flushCacheByUriPattern(Pattern urlPattern);

    /**
     * Retrieves HTTP content asynchronously using the given parameters.
     * {@code Parameters} will be added as query parameters for  {@code GET}, {@code DELETE} and {@code HEAD}.
     * If the Content-Type header is {@link ContentType#APPLICATION_FORM_URLENCODED},  then the parameters will also
     * be added to {@code POST}, {@code PUT} and {@code TRACE}.
     *
     * The {@code body} is then added as the entity for {@code POST}, {@code PUT} and {@code TRACE} methods, and ignored for
     * {@code GET}, {@code DELETE} and {@code HEAD}.
     * It is up to implementation to limit the list of accepted HTTP methods.
     *
     * @param authorizationGenerator the generator for the authorisation header
     * @param method                 the HTTP method to use
     * @param url                    the url to hit
     * @param parameters             the parameters to use.
     * @param headers                the headers to use. If 'Content-Type' is null, then defaults to {@link ContentType#APPLICATION_FORM_URLENCODED}
     * @param body                   the inputstream which returns the body, if method is POST, or PUT, or TRACE.
     *                               If 'Content-Type' is {@link ContentType#APPLICATION_FORM_URLENCODED}, then the {@code parameters} are
     *                               also converted into form-url-encoded format and appended to the body.
     * @param addOnKey               the key of the add-on from which to retrieve the content
     * @return a promise of the retrieved content
     */
    Promise<String> async(@Nonnull AuthorizationGenerator authorizationGenerator,
                          @Nonnull HttpMethod method,
                          @Nonnull URI url,
                          @Nonnull Map<String, String[]> parameters,
                          @Nonnull Map<String, String> headers,
                          @Nonnull InputStream body,
                          @Nonnull String addOnKey);

}
