package com.atlassian.plugin.remotable.plugin.util.http;

import com.atlassian.util.concurrent.Promise;

import java.net.URI;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Retrieves and caches http content.
 */
public interface HttpContentRetriever
{
    void flushCacheByUriPattern(Pattern urlPattern);

    Promise<String> getAsync(AuthorizationGenerator authorizationGenerator, String remoteUsername, URI url,
            Map<String, String> parameters,
            Map<String, String> headers, String moduleKey);
}
