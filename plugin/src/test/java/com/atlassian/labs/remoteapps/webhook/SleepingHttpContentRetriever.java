package com.atlassian.labs.remoteapps.webhook;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.labs.remoteapps.ContentRetrievalException;
import com.atlassian.labs.remoteapps.util.http.HttpContentHandler;
import com.atlassian.labs.remoteapps.util.http.HttpContentRetriever;

import java.util.Map;
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
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    public void getAsync(ApplicationLink link, String remoteUsername, String url, Map<String, String> parameters,
                         HttpContentHandler handler)
    {
        try
        {
            Thread.sleep(100000);
        } catch (InterruptedException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    public String get(ApplicationLink link, String remoteUsername, String url, Map<String, String> parameters) throws
                                                                                                               ContentRetrievalException
    {
        try
        {
            Thread.sleep(100000);
        } catch (InterruptedException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    @Override
    public void postIgnoreResponse(ApplicationLink link, String url, String jsonBody)
    {
        try
        {
            Thread.sleep(100000);
        } catch (InterruptedException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
