package com.atlassian.plugin.connect.test.common.servlet;

import java.net.HttpURLConnection;
import java.net.URI;

/**
 * Signs and validates requests.  Also provides access to base urls.
 */
public interface SignedRequestHandler
{
    void sign(URI uri, String method, String username, HttpURLConnection yc);
}
