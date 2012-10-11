package com.atlassian.plugin.remotable.api.service.http;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * Helper methods for working with response promises
 */
public final class ResponsePromises
{
    private ResponsePromises() { }

    /**
     * Returns a new promise representing the status of a list of other promises.
     * Status code or status code range callbacks only fire if all responses match.
     *
     * @param promises The promises that the new promise should track
     * @return The new, aggregate promise
     */
    public static ResponsesPromise when(ResponsePromise... promises)
    {
        return when(asList(promises));
    }

    /**
     * Returns a new promise representing the status of a list of other promises.
     * Status code or status code range callbacks only fire if all responses match.
     *
     * @param promises The promises that the new promise should track
     * @return The new, aggregate promise
     */
    public static ResponsesPromise when(Iterable<? extends ResponsePromise> promises)
    {
        return toResponsePromise(Futures.<Response>allAsList(promises));
    }

    public static ResponsePromise toResponsePromise(ListenableFuture<Response> future)
    {
        return new WrappingResponsePromise(future);
    }

    public static ResponsesPromise toResponsePromise(ListenableFuture<List<Response>> future)
    {
        return new WrappingResponsesPromise(future);
    }
}
