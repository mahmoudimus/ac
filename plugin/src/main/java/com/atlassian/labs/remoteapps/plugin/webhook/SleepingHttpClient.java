package com.atlassian.labs.remoteapps.plugin.webhook;

import com.atlassian.labs.remoteapps.api.service.http.Response;
import com.atlassian.labs.remoteapps.api.service.http.ResponsePromise;
import com.atlassian.labs.remoteapps.spi.http.ResponsePromises;
import com.atlassian.labs.remoteapps.host.common.service.http.AbstractHttpClient;
import com.atlassian.labs.remoteapps.host.common.service.http.DefaultRequest;
import com.google.common.util.concurrent.SettableFuture;

public class SleepingHttpClient extends AbstractHttpClient
{
    @Override
    protected ResponsePromise execute(DefaultRequest request)
    {
        try
        {
            Thread.sleep(100000);
            return ResponsePromises.ofFuture(SettableFuture.<Response>create());
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }
}
