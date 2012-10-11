package com.atlassian.plugin.remotable.plugin.webhook;

import com.atlassian.plugin.remotable.api.service.http.Response;
import com.atlassian.plugin.remotable.api.service.http.ResponsePromise;
import com.atlassian.plugin.remotable.host.common.service.http.AbstractHttpClient;
import com.atlassian.plugin.remotable.host.common.service.http.DefaultRequest;
import com.google.common.util.concurrent.SettableFuture;

import static com.atlassian.plugin.remotable.api.service.http.ResponsePromises.toResponsePromise;

public class SleepingHttpClient extends AbstractHttpClient
{
    @Override
    protected ResponsePromise execute(DefaultRequest request)
    {
        try
        {
            Thread.sleep(100000);
            return toResponsePromise(SettableFuture.<Response>create());
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }
}
