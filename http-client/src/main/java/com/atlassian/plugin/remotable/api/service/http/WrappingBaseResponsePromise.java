package com.atlassian.plugin.remotable.api.service.http;

import com.atlassian.util.concurrent.Effect;
import com.atlassian.util.concurrent.Promise;
import com.atlassian.util.concurrent.Promises;
import com.google.common.base.Function;
import com.google.common.util.concurrent.ForwardingListenableFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Arrays;
import java.util.Set;

import static com.google.common.collect.Sets.*;

abstract class WrappingBaseResponsePromise<V> extends ForwardingListenableFuture.SimpleForwardingListenableFuture<V> implements BaseResponsePromise<V>
{
    private final Set<Integer> statuses;
    private final Set<StatusSet> statusSets;

    public WrappingBaseResponsePromise(ListenableFuture<V> delegate)
    {
        super(Promises.forListenableFuture(delegate));
        this.statuses = newHashSet();
        this.statusSets = newHashSet();
    }

    @Override
    public final BaseResponsePromise<V> on(int statusCode, Effect<V> callback)
    {
        statuses.add(statusCode);
        onSuccess(newStatusSelector(statusCode, callback));
        return this;
    }

    @Override
    public final BaseResponsePromise<V> informational(Effect<V> callback)
    {
        return onRange(100, 200, callback);
    }

    @Override
    public final BaseResponsePromise<V> successful(Effect<V> callback)
    {
        return onRange(200, 300, callback);
    }

    @Override
    public final BaseResponsePromise<V> ok(Effect<V> callback)
    {
        return on(200, callback);
    }

    @Override
    public final BaseResponsePromise<V> created(Effect<V> callback)
    {
        return on(201, callback);
    }

    @Override
    public final BaseResponsePromise<V> noContent(Effect<V> callback)
    {
        return on(204, callback);
    }

    @Override
    public final BaseResponsePromise<V> redirection(Effect<V> callback)
    {
        return onRange(300, 400, callback);
    }

    @Override
    public final BaseResponsePromise<V> seeOther(Effect<V> callback)
    {
        return on(303, callback);
    }

    @Override
    public final BaseResponsePromise<V> notModified(Effect<V> callback)
    {
        return on(304, callback);
    }

    @Override
    public final BaseResponsePromise<V> clientError(Effect<V> callback)
    {
        return onRange(400, 500, callback);
    }

    @Override
    public final BaseResponsePromise<V> badRequest(Effect<V> callback)
    {
        return on(400, callback);
    }

    @Override
    public final BaseResponsePromise<V> unauthorized(Effect<V> callback)
    {
        return on(401, callback);
    }

    @Override
    public final BaseResponsePromise<V> forbidden(Effect<V> callback)
    {
        return on(403, callback);
    }

    @Override
    public final BaseResponsePromise<V> notFound(Effect<V> callback)
    {
        return on(404, callback);
    }

    @Override
    public final BaseResponsePromise<V> conflict(Effect<V> callback)
    {
        return on(409, callback);
    }

    @Override
    public final BaseResponsePromise<V> serverError(Effect<V> callback)
    {
        return onRange(500, 600, callback);
    }

    @Override
    public final BaseResponsePromise<V> internalServerError(Effect<V> callback)
    {
        return on(500, callback);
    }

    @Override
    public final BaseResponsePromise<V> serviceUnavailable(Effect<V> callback)
    {
        return on(503, callback);
    }

    @Override
    public final BaseResponsePromise<V> error(Effect<V> callback)
    {
        clientError(callback);
        serverError(callback);
        return this;
    }

    @Override
    public final BaseResponsePromise<V> notSuccessful(Effect<V> callback)
    {
        MultiRange multi = new MultiRange(new Range(100, 200), new Range(300, 600));
        statusSets.add(multi);
        onSuccess(newStatusSetSelector(multi, callback));
        return this;
    }

    @Override
    public final BaseResponsePromise<V> others(Effect<V> callback)
    {
        onSuccess(newOthersSelector(statuses, statusSets, callback));
        return this;
    }

    @Override
    public final V claim()
    {
        return delegatePromise().claim();
    }

    @Override
    public final BaseResponsePromise<V> onSuccess(Effect<V> callback)
    {
        delegatePromise().onSuccess(callback);
        return this;
    }

    @Override
    public final BaseResponsePromise<V> onFailure(Effect<Throwable> callback)
    {
        delegatePromise().onFailure(callback);
        return this;
    }

    @Override
    public final BaseResponsePromise<V> on(FutureCallback<V> callback)
    {
        delegatePromise().on(callback);
        return this;
    }

    @Override
    public final <T> Promise<T> map(Function<? super V, ? extends T> function)
    {
        return Promises.forListenableFuture(Futures.transform(delegate(), function));
    }

    @Override
    public final <T> Promise<T> flatMap(Function<? super V, Promise<T>> function)
    {
        return Promises.forListenableFuture(Futures.chain(delegate(), function));
    }

    protected final Promise<V> delegatePromise()
    {
        return (Promise<V>) delegate();
    }

    protected abstract Effect<V> newStatusSelector(int statusCode, Effect<V> callback);

    protected abstract Effect<V> newStatusSetSelector(StatusSet statusSet, Effect<V> callback);

    protected abstract Effect<V> newOthersSelector(Set<Integer> statuses, Set<StatusSet> statusSets, Effect<V> callback);

    private BaseResponsePromise<V> onRange(int lower, int upper, Effect<V> callback)
    {
        Range range = new Range(lower, upper);
        statusSets.add(range);
        onSuccess(newStatusSetSelector(range, callback));
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
