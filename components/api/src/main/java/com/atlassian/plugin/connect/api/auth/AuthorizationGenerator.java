package com.atlassian.plugin.connect.api.auth;

import com.atlassian.plugin.connect.api.request.HttpMethod;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

/**
 * Generates an authorization http header value
 */
public interface AuthorizationGenerator {
    /**
     * Generates an authorisation header for the request with the given parameters.
     *
     * @param method     the {@link HttpMethod HTTP method} used
     * @param url        the url of the HTTP request
     * @param parameters the parameters of the HTTP request
     * @return some authorisation header, none if not generated.
     * @since 0.10
     */
    Optional<String> generate(HttpMethod method, URI url, Map<String, String[]> parameters);
}
