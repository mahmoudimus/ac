package com.atlassian.labs.remoteapps.spi.http;

import com.atlassian.labs.remoteapps.api.Promise;
import com.atlassian.labs.remoteapps.api.PromiseCallback;
import com.atlassian.labs.remoteapps.api.service.http.BaseResponsePromise;
import com.google.common.util.concurrent.ForwardingListenableFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.atlassian.labs.remoteapps.api.Promises.toPromise;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

public abstract class WrappingBaseResponsePromise<V> extends ForwardingListenableFuture.SimpleForwardingListenableFuture<V> implements BaseResponsePromise<V>
{
    private final Set<Integer> statuses;
    private final Set<StatusSet> statusSets;

    public WrappingBaseResponsePromise(ListenableFuture<V> delegate)
    {
        super(toPromise(delegate));
        this.statuses = newHashSet();
        this.statusSets = newHashSet();
    }

    @Override
    public BaseResponsePromise<V> on(int statusCode, PromiseCallback<V> callback)
    {
        statuses.add(statusCode);
        done(newStatusSelector(statusCode, callback));
        return this;
    }

    @Override
    public BaseResponsePromise<V> informational(PromiseCallback<V> callback)
    {
        return onRange(100, 200, callback);
    }

    @Override
    public BaseResponsePromise<V> successful(PromiseCallback<V> callback)
    {
        return onRange(200, 300, callback);
    }

    @Override
    public BaseResponsePromise<V> ok(PromiseCallback<V> callback)
    {
        return on(200, callback);
    }

    @Override
    public BaseResponsePromise<V> created(PromiseCallback<V> callback)
    {
        return on(201, callback);
    }

    @Override
    public BaseResponsePromise<V> noContent(PromiseCallback<V> callback)
    {
        return on(204, callback);
    }

    @Override
    public BaseResponsePromise<V> redirection(PromiseCallback<V> callback)
    {
        return onRange(300, 400, callback);
    }

    @Override
    public BaseResponsePromise<V> seeOther(PromiseCallback<V> callback)
    {
        return on(303, callback);
    }

    @Override
    public BaseResponsePromise<V> notModified(PromiseCallback<V> callback)
    {
        return on(304, callback);
    }

    @Override
    public BaseResponsePromise<V> clientError(PromiseCallback<V> callback)
    {
        return onRange(400, 500, callback);
    }

    @Override
    public BaseResponsePromise<V> badRequest(PromiseCallback<V> callback)
    {
        return on(400, callback);
    }

    @Override
    public BaseResponsePromise<V> unauthorized(PromiseCallback<V> callback)
    {
        return on(401, callback);
    }

    @Override
    public BaseResponsePromise<V> forbidden(PromiseCallback<V> callback)
    {
        return on(403, callback);
    }

    @Override
    public BaseResponsePromise<V> notFound(PromiseCallback<V> callback)
    {
        return on(404, callback);
    }

    @Override
    public BaseResponsePromise<V> conflict(PromiseCallback<V> callback)
    {
        return on(409, callback);
    }

    @Override
    public BaseResponsePromise<V> serverError(PromiseCallback<V> callback)
    {
        return onRange(500, 600, callback);
    }

    @Override
    public BaseResponsePromise<V> internalServerError(PromiseCallback<V> callback)
    {
        return on(500, callback);
    }

    @Override
    public BaseResponsePromise<V> serviceUnavailable(PromiseCallback<V> callback)
    {
        return on(503, callback);
    }

    @Override
    public BaseResponsePromise<V> error(PromiseCallback<V> callback)
    {
        clientError(callback);
        serverError(callback);
        return this;
    }

    @Override
    public BaseResponsePromise<V> notSuccessful(PromiseCallback<V> callback)
    {
        MultiRange multi = new MultiRange(new Range(100, 200), new Range(300, 600));
        statusSets.add(multi);
        done(newStatusSetSelector(multi, callback));
        return this;
    }

    @Override
    public BaseResponsePromise<V> others(PromiseCallback<V> callback)
    {
        done(newOthersSelector(statuses, statusSets, callback));
        return this;
    }

    @Override
    public V claim()
    {
        return delegatePromise().claim();
    }

    @Override
    public BaseResponsePromise<V> done(PromiseCallback<V> callback)
    {
        delegatePromise().done(callback);
        return this;
    }

    @Override
    public BaseResponsePromise<V> fail(PromiseCallback<Throwable> callback)
    {
        delegatePromise().fail(callback);
        return this;
    }

    @Override
    public BaseResponsePromise<V> then(FutureCallback<V> callback)
    {
        delegatePromise().then(callback);
        return this;
    }

    protected Promise<V> delegatePromise()
    {
        return (Promise<V>) delegate();
    }

    protected abstract PromiseCallback<V> newStatusSelector(int statusCode, PromiseCallback<V> callback);

    protected abstract PromiseCallback<V> newStatusSetSelector(StatusSet statusSet, PromiseCallback<V> callback);

    protected abstract PromiseCallback<V> newOthersSelector(Set<Integer> statuses, Set<StatusSet> statusSets, PromiseCallback<V> callback);

    private BaseResponsePromise<V> onRange(int lower, int upper, PromiseCallback<V> callback)
    {
        Range range = new Range(lower, upper);
        statusSets.add(range);
        done(newStatusSetSelector(range, callback));
        return this;
    }

    protected static interface StatusSet
    {
        boolean contains(int value);
    }

    protected static class Range implements StatusSet
    {
        private final int lowerBoundInclusive;
        private final int upperBoundExclusive;

        private Range(int lowerBoundInclusive, int upperBoundExclusive)
        {
            this.lowerBoundInclusive = lowerBoundInclusive;
            this.upperBoundExclusive = upperBoundExclusive;
        }

        public boolean contains(int value)
        {
            return value >= lowerBoundInclusive && value < upperBoundExclusive;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }
            Range range = (Range) o;
            return lowerBoundInclusive == range.lowerBoundInclusive
                && upperBoundExclusive == range.upperBoundExclusive;
        }

        @Override
        public int hashCode()
        {
            int result = lowerBoundInclusive;
            result = 31 * result + upperBoundExclusive;
            return result;
        }
    }

    protected static class MultiRange implements StatusSet
    {
        private Range[] ranges;

        private MultiRange(Range... ranges)
        {
            this.ranges = ranges;
        }

        public boolean contains(int value)
        {
            for (Range range : ranges)
            {
                if (range.contains(value))
                {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }
            MultiRange that = (MultiRange) o;
            return Arrays.equals(ranges, that.ranges);
        }

        @Override
        public int hashCode()
        {
            return ranges != null ? Arrays.hashCode(ranges) : 0;
        }
    }
}
