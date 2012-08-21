package com.atlassian.labs.remoteapps.plugin.util.http;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

/**
 * Retrieves and caches http content.
 */
public interface HttpContentRetriever
{
    void flushCacheByUrlPattern(Pattern urlPattern);

    Future<String> getAsync(AuthorizationGenerator authorizationGenerator, String remoteUsername, String url,
            Map<String, String> parameters,
            Map<String, String> headers, HttpContentHandler handler, String moduleKey);
}
