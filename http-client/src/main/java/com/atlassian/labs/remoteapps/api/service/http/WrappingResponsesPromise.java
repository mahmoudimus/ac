package com.atlassian.labs.remoteapps.api.service.http;

import com.atlassian.util.concurrent.Effect;
import com.atlassian.util.concurrent.Promises;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Extends WrappingBaseResponsePromise with the ResponsesPromise interface
 */
final class WrappingResponsesPromise extends WrappingBaseResponsePromise<List<Response>> implements ResponsesPromise
{
    public WrappingResponsesPromise(ListenableFuture<List<Response>> delegate)
    {
        super(Promises.forListenableFuture(delegate));
    }

    @Override
    public BaseResponsePromise<List<Response>> otherwise(final Effect<Throwable> callback)
    {
        others(new Effect<List<Response>>()
        {
            @Override
            public void apply(List<Response> responses)
            {
                callback.apply(new UnexpectedResponsesException(responses));
            }
        });
        onFailure(callback);
        return this;
    }

    @Override
    protected Effect<List<Response>> newStatusSelector(int statusCode, Effect<List<Response>> callback)
    {
        return new StatusSelector(statusCode, callback);
    }

    @Override
    protected Effect<List<Response>> newStatusSetSelector(StatusSet statusSet, Effect<List<Response>> callback)
    {
        return new StatusSetSelector(statusSet, callback);
    }

    @Override
    protected Effect<List<Response>> newOthersSelector(Set<Integer> statuses, Set<StatusSet> statusSets, Effect<List<Response>> callback)
    {
        return new OthersSelector(statuses, statusSets, callback);
    }

    private static class StatusSelector implements Effect<List<Response>>
    {
        private final int statusCode;
        private final Effect<List<Response>> callback;

        private StatusSelector(int statusCode, Effect<List<Response>> callback)
        {
            this.statusCode = statusCode;
            this.callback = callback;
        }

        @Override
        public void apply(List<Response> responses)
        {
            boolean allMatch = true;
            for (Response response : responses)
            {
                if (response.getStatusCode() != statusCode)
                {
                    allMatch = false;
                    break;
                }
            }
            if (allMatch)
            {
                callback.apply(responses);
            }
        }
    }

    private static class StatusSetSelector implements Effect<List<Response>>
    {
        private StatusSet statusSet;
        private final Effect<List<Response>> callback;

        private StatusSetSelector(StatusSet statusSet, Effect<List<Response>> callback)
        {
            this.statusSet = statusSet;
            this.callback = callback;
        }

        @Override
        public void apply(List<Response> responses)
        {
            boolean allMatch = true;
            for (Response response : responses)
            {
                if (!statusSet.contains(response.getStatusCode()))
                {
                    allMatch = false;
                    break;
                }
            }
            if (allMatch)
            {
                callback.apply(responses);
            }
        }
    }

    private static class OthersSelector implements Effect<List<Response>>
    {
        private final Effect<List<Response>> callback;
        private final Set<Integer> statuses;
        private final Set<StatusSet> statusSets;

        private OthersSelector(Set<Integer> statuses, Set<StatusSet> statusSets, Effect<List<Response>> callback)
        {
            this.statuses = new HashSet<Integer>(statuses);
            this.statusSets = new HashSet<StatusSet>(statusSets);
            this.callback = callback;
        }

        @Override
        public void apply(List<Response> responses)
        {
            boolean noneMatch = true;
            for (StatusSet statusSet : statusSets)
            {
                if (containsAll(statusSet, responses))
                {
                    noneMatch = false;
                    break;
                }
            }
            if (noneMatch)
            {
                for (int status : statuses)
                {
                    if (matchesAll(status, responses))
                    {
                        noneMatch = false;
                        break;
                    }
                }
                if (noneMatch)
                {
                    callback.apply(responses);
                }
            }
        }

        private boolean containsAll(StatusSet statusSet, List<Response> responses)
        {
            boolean result = true;
            for (Response response : responses)
            {
                if (!statusSet.contains(response.getStatusCode()))
                {
                    result = false;
                    break;
                }
            }
            return result;
        }

        private boolean matchesAll(int status, List<Response> responses)
        {
            boolean result = true;
            for (Response response : responses)
            {
                if (response.getStatusCode() != status)
                {
                    result = false;
                    break;
                }
            }
            return result;
        }
    }
}
