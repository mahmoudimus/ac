package com.atlassian.plugin.connect.spi.http;

import java.net.URI;
import java.util.List;
import java.util.Map;

import com.atlassian.fugue.Option;

/**
 * Generates an authorization http header value
 */
public interface AuthorizationGenerator
{
    /**
     * Generates an authorisation header for the request with the given parameters.
     *
     * @param method     the name of the HTTP method used
     * @param url        the url of the HTTP request
     * @param parameters the parameters of the HTTP request
     * @return an authorisation header, {@code null} if none was generated.
     * @deprecated since 0.10
     */
    @Deprecated
    String generate(String method, URI url, Map<String, List<String>> parameters);

    /**
     * Generates an authorisation header for the request with the given parameters.
     *
     * @param method     the {@link HttpMethod HTTP method} used
     * @param url        the url of the HTTP request
     * @param parameters the parameters of the HTTP request
     * @return some authorisation header, none if not generated.
     * @since 0.10
     */
    Option<String> generate(HttpMethod method, URI url, Map<String, List<String>> parameters);
}
