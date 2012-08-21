package com.atlassian.labs.remoteapps.plugin.webhook;

import com.atlassian.labs.remoteapps.api.Promise;
import com.atlassian.labs.remoteapps.host.common.service.http.AbstractHttpClient;
import com.atlassian.labs.remoteapps.spi.WrappingPromise;
import com.google.common.util.concurrent.SettableFuture;
import org.apache.http.HttpResponse;

import java.io.InputStream;
import java.util.Map;

public class SleepingHttpClient extends AbstractHttpClient
{
    @Override
    public Promise<HttpResponse> request(Method method, String uri, Map<String, String> headers, InputStream entity, Map<String, String> properties)
    {
        try
        {
            Thread.sleep(100000);
            return new WrappingPromise<HttpResponse>(SettableFuture.<HttpResponse>create());
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }
}
