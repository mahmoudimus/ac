package com.atlassian.labs.remoteapps.api.service.http;

import com.atlassian.util.concurrent.Effect;
import com.atlassian.util.concurrent.Promises;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.HashSet;
import java.util.Set;

/**
 * Extends WrappingBaseResponsePromise with the ResponsePromise interface
 */
final class WrappingResponsePromise extends WrappingBaseResponsePromise<Response> implements ResponsePromise
{
    public WrappingResponsePromise(ListenableFuture<Response> delegate)
    {
        super(Promises.forListenableFuture(delegate));
    }

    @Override
    public BaseResponsePromise<Response> otherwise(final Effect<Throwable> callback)
    {
        others(new Effect<Response>()
        {
            @Override
            public void apply(Response response)
            {
                callback.apply(new UnexpectedResponseException(response));
            }
        });
        onFailure(callback);
        return this;
    }

    @Override
    protected Effect<Response> newStatusSelector(int statusCode, Effect<Response> callback)
    {
        return new StatusSelector(statusCode, callback);
    }

    @Override
    protected Effect<Response> newStatusSetSelector(StatusSet statusSet, Effect<Response> callback)
    {
        return new StatusSetSelector(statusSet, callback);
    }

    @Override
    protected Effect<Response> newOthersSelector(Set<Integer> statuses, Set<StatusSet> statusSets, Effect<Response> callback)
    {
        return new OthersSelector(statuses, statusSets, callback);
    }

    private static class StatusSelector implements Effect<Response>
    {
        private final int statusCode;
        private final Effect<Response> callback;

        private StatusSelector(int statusCode, Effect<Response> callback)
        {
            this.statusCode = statusCode;
            this.callback = callback;
        }

        @Override
        public void apply(Response response)
        {
            if (response.getStatusCode() == statusCode)
            {
                callback.apply(response);
            }
        }
    }

    private static class StatusSetSelector implements Effect<Response>
    {
        private StatusSet statusSet;
        private final Effect<Response> callback;

        private StatusSetSelector(StatusSet statusSet, Effect<Response> callback)
        {
            this.statusSet = statusSet;
            this.callback = callback;
        }

        @Override
        public void apply(Response response)
        {
            if (statusSet.contains(response.getStatusCode()))
            {
                callback.apply(response);
            }
        }
    }

    private static class OthersSelector implements Effect<Response>
    {
        private final Effect<Response> callback;
        private final Set<Integer> statuses;
        private final Set<StatusSet> statusSets;

        private OthersSelector(Set<Integer> statuses, Set<StatusSet> statusSets, Effect<Response> callback)
        {
            this.statuses = new HashSet<Integer>(statuses);
            this.statusSets = new HashSet<StatusSet>(statusSets);
            this.callback = callback;
        }

        @Override
        public void apply(Response response)
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
                callback.apply(response);
            }
        }
    }
}
