package com.atlassian.labs.remoteapps.spi.http;

import com.atlassian.labs.remoteapps.api.PromiseCallback;
import com.atlassian.labs.remoteapps.api.service.http.BaseResponsePromise;
import com.atlassian.labs.remoteapps.api.service.http.Response;
import com.atlassian.labs.remoteapps.api.service.http.ResponsesPromise;
import com.atlassian.labs.remoteapps.api.service.http.UnexpectedResponsesException;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.atlassian.labs.remoteapps.api.Promises.toPromise;

/**
 * Extends WrappingBaseResponsePromise with the ResponsesPromise interface
 */
public final class WrappingResponsesPromise extends WrappingBaseResponsePromise<List<Response>> implements ResponsesPromise
{
    public WrappingResponsesPromise(ListenableFuture<List<Response>> delegate)
    {
        super(toPromise(delegate));
    }

    @Override
    public BaseResponsePromise<List<Response>> otherwise(final PromiseCallback<Throwable> callback)
    {
        others(new PromiseCallback<List<Response>>()
        {
            @Override
            public void handle(List<Response> responses)
            {
                callback.handle(new UnexpectedResponsesException(responses));
            }
        });
        fail(callback);
        return this;
    }

    @Override
    protected PromiseCallback<List<Response>> newStatusSelector(int statusCode, PromiseCallback<List<Response>> callback)
    {
        return new StatusSelector(statusCode, callback);
    }

    @Override
    protected PromiseCallback<List<Response>> newStatusSetSelector(StatusSet statusSet, PromiseCallback<List<Response>> callback)
    {
        return new StatusSetSelector(statusSet, callback);
    }

    @Override
    protected PromiseCallback<List<Response>> newOthersSelector(Set<Integer> statuses, Set<StatusSet> statusSets, PromiseCallback<List<Response>> callback)
    {
        return new OthersSelector(statuses, statusSets, callback);
    }

    private static class StatusSelector implements PromiseCallback<List<Response>>
    {
        private final int statusCode;
        private final PromiseCallback<List<Response>> callback;

        private StatusSelector(int statusCode, PromiseCallback<List<Response>> callback)
        {
            this.statusCode = statusCode;
            this.callback = callback;
        }

        @Override
        public void handle(List<Response> responses)
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
                callback.handle(responses);
            }
        }
    }

    private static class StatusSetSelector implements PromiseCallback<List<Response>>
    {
        private StatusSet statusSet;
        private final PromiseCallback<List<Response>> callback;

        private StatusSetSelector(StatusSet statusSet, PromiseCallback<List<Response>> callback)
        {
            this.statusSet = statusSet;
            this.callback = callback;
        }

        @Override
        public void handle(List<Response> responses)
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
                callback.handle(responses);
            }
        }
    }

    private static class OthersSelector implements PromiseCallback<List<Response>>
    {
        private final PromiseCallback<List<Response>> callback;
        private final Set<Integer> statuses;
        private final Set<StatusSet> statusSets;

        private OthersSelector(Set<Integer> statuses, Set<StatusSet> statusSets, PromiseCallback<List<Response>> callback)
        {
            this.statuses = new HashSet<Integer>(statuses);
            this.statusSets = new HashSet<StatusSet>(statusSets);
            this.callback = callback;
        }

        @Override
        public void handle(List<Response> responses)
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
                    callback.handle(responses);
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
