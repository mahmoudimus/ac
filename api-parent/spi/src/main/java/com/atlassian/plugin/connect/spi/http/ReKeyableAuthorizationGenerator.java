package com.atlassian.plugin.connect.spi.http;

import com.atlassian.plugin.connect.api.http.HttpMethod;

import java.net.URI;
import java.util.Map;

/**
 * Generates an authorization http header value, optionally using an arbitrary secret.
 */
public interface ReKeyableAuthorizationGenerator extends AuthorizationGenerator
{
    /**
     * Generates an authorisation header for the request with the given parameters.
     *
     * @param method     the {@link com.atlassian.plugin.connect.api.http.HttpMethod HTTP method} used
     * @param url        the url of the HTTP request
     * @param parameters the parameters of the HTTP request
     * @param secret    secret with which to sign (e.g. JWT shared secret or private key)
     * @return some authorisation header, none if not generated.
     * @since 0.10
     */
    String generate(HttpMethod method, URI url, Map<String, String[]> parameters, String secret);
}
