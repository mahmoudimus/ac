package com.atlassian.labs.remoteapps.api.services;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.net.HttpURLConnection;

/**
 * Created with IntelliJ IDEA. User: mrdon Date: 7/27/12 Time: 1:29 PM To change this template use
 * File | Settings | File Templates.
 */
public interface SignedRequestHandler
{
    String getHostBaseUrl(String key);

    String getLocalBaseUrl();

    String validateRequest(HttpServletRequest req) throws ServletException;

    void sign(String uri, String method, String username, HttpURLConnection yc);

    String getAuthorizationHeaderValue(String uri, String method, String username)
            throws IllegalArgumentException;
}
