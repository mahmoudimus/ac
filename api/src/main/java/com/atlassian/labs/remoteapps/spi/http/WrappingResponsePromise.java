package com.atlassian.labs.remoteapps.spi.http;

import com.atlassian.labs.remoteapps.api.PromiseCallback;
import com.atlassian.labs.remoteapps.api.service.http.Response;
import com.atlassian.labs.remoteapps.api.service.http.ResponsePromise;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.HashSet;
import java.util.Set;

import static com.atlassian.labs.remoteapps.api.Promises.toPromise;

/**
 * Extends WrappingBaseResponsePromise with the ResponsePromise interface
 */
public final class WrappingResponsePromise extends WrappingBaseResponsePromise<Response> implements ResponsePromise
{
    public WrappingResponsePromise(ListenableFuture<Response> delegate)
    {
        super(toPromise(delegate));
    }

    @Override
    protected PromiseCallback<Response> newStatusSelector(int statusCode, PromiseCallback<Response> callback)
    {
        return new StatusSelector(statusCode, callback);
    }

    @Override
    protected PromiseCallback<Response> newStatusSetSelector(StatusSet statusSet, PromiseCallback<Response> callback)
    {
        return new StatusSetSelector(statusSet, callback);
    }

    @Override
    protected PromiseCallback<Response> newOthersSelector(Set<Integer> statuses, Set<StatusSet> statusSets, PromiseCallback<Response> callback)
    {
        return new OthersSelector(statuses, statusSets, callback);
    }

    private class StatusSelector implements PromiseCallback<Response>
    {
        private final int statusCode;
        private final PromiseCallback<Response> callback;

        private StatusSelector(int statusCode, PromiseCallback<Response> callback)
        {
            this.statusCode = statusCode;
            this.callback = callback;
        }

        @Override
        public void handle(Response response)
        {
            if (response.getStatusCode() == statusCode)
            {
                callback.handle(response);
            }
        }
    }

    private class StatusSetSelector implements PromiseCallback<Response>
    {
        private StatusSet statusSets;
        private final PromiseCallback<Response> callback;

        private StatusSetSelector(StatusSet statusSets, PromiseCallback<Response> callback)
        {
            this.statusSets = statusSets;
            this.callback = callback;
        }

        @Override
        public void handle(Response response)
        {
            if (statusSets.contains(response.getStatusCode()))
            {
                callback.handle(response);
            }
        }
    }

    private class OthersSelector implements PromiseCallback<Response>
    {
        private final PromiseCallback<Response> callback;
        private final Set<Integer> statuses;
        private final Set<StatusSet> statusSets;

        private OthersSelector(Set<Integer> statuses, Set<StatusSet> statusSets, PromiseCallback<Response> callback)
        {
            this.statuses = new HashSet<Integer>(statuses);
            this.statusSets = new HashSet<StatusSet>(statusSets);
            this.callback = callback;
        }

        @Override
        public void handle(Response response)
        {
            int status = response.getStatusCode();
            boolean inStatusSets = false;
            for (StatusSet statusSet : statusSets)
            {
                if (statusSet.contains(status))
                {
                    inStatusSets = true;
                    break;
                }
            }
            if (!inStatusSets && !statuses.contains(status))
            {
                callback.handle(response);
            }
        }
    }
}
