package com.atlassian.plugin.remotable.spi.http;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Generates an authorization http header value
 */
public interface AuthorizationGenerator
{
    String generate(String method, URI url, Map<String,List<String>> parameters);
}
