package com.atlassian.labs.remoteapps.util.http;

import java.util.List;
import java.util.Map;

/**
 * Generates an authorization http header value
 */
public interface AuthorizationGenerator
{
    String generate(String method, String url, Map<String,List<String>> parameters);
}
