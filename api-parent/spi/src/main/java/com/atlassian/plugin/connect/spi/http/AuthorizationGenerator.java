package com.atlassian.plugin.connect.spi.http;

import com.atlassian.fugue.Option;

import java.net.URI;
import java.util.Map;

/**
 * Generates an authorization http header value
 */
public interface AuthorizationGenerator
{
    /**
     * Generates an authorisation header for the request with the given parameters.
     *
     * @param method     the {@link HttpMethod HTTP method} used
     * @param url        the url of the HTTP request
     * @param baseUrl    the prefix of all HTTP requests to this addOn, as specified in its descriptor
     * @param parameters the parameters of the HTTP request
     * @return some authorisation header, none if not generated.
     * @since 1.0.2
     */
    Option<String> generate(HttpMethod method, URI url, URI baseUrl, Map<String, String[]> parameters);
}
