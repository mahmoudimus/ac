package com.atlassian.labs.remoteapps.webhook;

import com.atlassian.labs.remoteapps.api.services.http.impl.AbstractAsyncHttpClient;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.apache.http.HttpResponse;

import java.io.InputStream;
import java.util.Map;

public class SleepingAsyncHttpClient extends AbstractAsyncHttpClient
{
    @Override
    public ListenableFuture<HttpResponse> request(Method method, String uri, Map<String, String> headers, InputStream entity, Map<String, String> properties)
    {
        try
        {
            Thread.sleep(100000);
            return SettableFuture.create();
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }
}
