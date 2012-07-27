package com.atlassian.labs.remoteapps.webhook;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.labs.remoteapps.ContentRetrievalException;
import com.atlassian.labs.remoteapps.util.http.AuthorizationGenerator;
import com.atlassian.labs.remoteapps.util.http.HttpContentHandler;
import com.atlassian.labs.remoteapps.util.http.HttpContentRetriever;
import com.atlassian.util.concurrent.SettableFuture;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

public class SleepingHttpContentRetriever implements HttpContentRetriever
{
    @Override
    public void flushCacheByUrlPattern(Pattern urlPattern)
    {
        try
        {
            Thread.sleep(100000);
        } catch (InterruptedException e)
        {
            // ignore
        }
    }

    @Override
    public Future<String> getAsync(AuthorizationGenerator authorizationGenerator, String remoteUsername, String url,
            Map<String, String> parameters, Map<String, String> headers, HttpContentHandler handler)
    {
        try
        {
            Thread.sleep(100000);
            return new SettableFuture<String>();
        } catch (InterruptedException e)
        {
            throw new ContentRetrievalException(e);
        }
    }

    @Override
    public String get(AuthorizationGenerator authorizationGenerator, String remoteUsername, String url, Map<String, String> parameters) throws
                                                                                                               ContentRetrievalException
    {
        try
        {
            Thread.sleep(100000);
        } catch (InterruptedException e)
        {
            // ignore
        }
        return null;
    }

    @Override
    public void postIgnoreResponse(AuthorizationGenerator authorizationGenerator, String url, String jsonBody)
    {
        try
        {
            Thread.sleep(100000);
        } catch (InterruptedException e)
        {
            // ignore
        }
    }
}
