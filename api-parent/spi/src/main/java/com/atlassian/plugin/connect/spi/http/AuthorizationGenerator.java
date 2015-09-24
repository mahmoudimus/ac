package com.atlassian.plugin.connect.spi.http;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.api.http.HttpMethod;
import com.atlassian.sal.api.user.UserProfile;

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
     * @param method     the {@link com.atlassian.plugin.connect.api.http.HttpMethod HTTP method} used
     * @param url        the url of the HTTP request
     * @param parameters the parameters of the HTTP request
     * @param remoteUser the user profile of the logged in user generating the authorization value
     * @return some authorisation header, none if not generated.
     * @since 0.10
     */
    Option<String> generate(HttpMethod method, URI url, Map<String, String[]> parameters, UserProfile remoteUser);
}
