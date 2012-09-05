package com.atlassian.labs.remoteapps.api.service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.net.HttpURLConnection;
import java.net.URI;

/**
 * Signs and validates requests.  Also provides access to base urls.
 */
public interface SignedRequestHandler
{
    String getHostBaseUrl(String key);

    String getLocalBaseUrl();

    String validateRequest(HttpServletRequest req) throws ServletException;

    void sign(URI uri, String method, String username, HttpURLConnection yc);

    String getAuthorizationHeaderValue(URI uri, String method, String username)
            throws IllegalArgumentException;
}
