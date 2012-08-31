package com.atlassian.labs.remoteapps.spi.http;

import com.atlassian.labs.remoteapps.api.service.http.Response;
import com.atlassian.labs.remoteapps.api.service.http.ResponsePromise;
import com.google.common.util.concurrent.ListenableFuture;

public final class ResponsePromises
{
    private ResponsePromises()
    {
    }

    public static ResponsePromise ofFuture(ListenableFuture<Response> future)
    {
        return new WrappingResponsePromise(future);
    }
}
