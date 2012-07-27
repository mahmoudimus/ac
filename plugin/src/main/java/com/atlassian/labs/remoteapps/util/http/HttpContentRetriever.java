package com.atlassian.labs.remoteapps.util.http;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.labs.remoteapps.ContentRetrievalException;
import com.atlassian.oauth.ServiceProvider;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

/**
 * Retrieves http content
 */
public interface HttpContentRetriever
{
    void flushCacheByUrlPattern(Pattern urlPattern);

    Future<String> getAsync(AuthorizationGenerator authorizationGenerator, String remoteUsername, String url,
            Map<String, String> parameters,
            Map<String, String> headers, HttpContentHandler handler);

    String get(AuthorizationGenerator authorizationGenerator, String remoteUsername, String url, Map<String, String> parameters) throws
                                                                                                        ContentRetrievalException;

    void postIgnoreResponse(AuthorizationGenerator authorizationGenerator, String url, String jsonBody);
}
