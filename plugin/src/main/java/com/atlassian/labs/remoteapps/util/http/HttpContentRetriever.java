package com.atlassian.labs.remoteapps.util.http;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.labs.remoteapps.ContentRetrievalException;
import com.atlassian.labs.remoteapps.util.http.HttpContentHandler;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: mrdon
 * Date: 21/01/12
 * Time: 7:50 PM
 * To change this template use File | Settings | File Templates.
 */
public interface HttpContentRetriever
{
    void flushCacheByUrlPattern(Pattern urlPattern);

    void getAsync(ApplicationLink link, String remoteUsername, String url, Map<String, String> parameters,
                  HttpContentHandler handler);

    String get(ApplicationLink link, String remoteUsername, String url, Map<String, String> parameters) throws
                                                                                                        ContentRetrievalException;

    void postIgnoreResponse(ApplicationLink link, String url, String jsonBody);
}
