package com.atlassian.labs.remoteapps.spi.http;

import com.atlassian.labs.remoteapps.api.PromiseCallback;
import com.atlassian.labs.remoteapps.api.service.http.Response;
import com.atlassian.labs.remoteapps.api.service.http.ResponsePromise;
import com.atlassian.labs.remoteapps.spi.WrappingPromise;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

/**
 * Extends WrappingPromise with the ResponsePromise interface
 */
public class WrappingResponsePromise extends WrappingPromise<Response> implements ResponsePromise
{
    private final Set<Integer> statuses;
    private final Set<Range> ranges;

    public WrappingResponsePromise(ListenableFuture<Response> delegate)
    {
        super(delegate);
        statuses = newHashSet();
        ranges = newHashSet();
    }

    @Override
    public ResponsePromise on(int statusCode, PromiseCallback<Response> callback)
    {
        done(new StatusSelector(statusCode, callback));
        return this;
    }

    private ResponsePromise onRange(int lower, int upper, PromiseCallback<Response> callback)
    {
        done(new StatusRangeSelector(new Range(lower, upper), callback));
        return this;
    }

    @Override
    public ResponsePromise informational(PromiseCallback<Response> callback)
    {
        return onRange(100, 200, callback);
    }

    @Override
    public ResponsePromise successful(PromiseCallback<Response> callback)
    {
        return onRange(200, 300, callback);
    }

    @Override
    public ResponsePromise ok(PromiseCallback<Response> callback)
    {
        return on(200, callback);
    }

    @Override
    public ResponsePromise created(PromiseCallback<Response> callback)
    {
        return on(201, callback);
    }

    @Override
    public ResponsePromise noContent(PromiseCallback<Response> callback)
    {
        return on(204, callback);
    }

    @Override
    public ResponsePromise redirection(PromiseCallback<Response> callback)
    {
        return onRange(300, 400, callback);
    }

    @Override
    public ResponsePromise seeOther(PromiseCallback<Response> callback)
    {
        return on(303, callback);
    }

    @Override
    public ResponsePromise notModified(PromiseCallback<Response> callback)
    {
        return on(304, callback);
    }

    @Override
    public ResponsePromise clientError(PromiseCallback<Response> callback)
    {
        return onRange(400, 500, callback);
    }

    @Override
    public ResponsePromise badRequest(PromiseCallback<Response> callback)
    {
        return on(400, callback);
    }

    @Override
    public ResponsePromise unauthorized(PromiseCallback<Response> callback)
    {
        return on(401, callback);
    }

    @Override
    public ResponsePromise forbidden(PromiseCallback<Response> callback)
    {
        return on(403, callback);
    }

    @Override
    public ResponsePromise conflict(PromiseCallback<Response> callback)
    {
        return on(409, callback);
    }

    @Override
    public ResponsePromise serverError(PromiseCallback<Response> callback)
    {
        return onRange(500, 600, callback);
    }

    @Override
    public ResponsePromise internalServerError(PromiseCallback<Response> callback)
    {
        return on(500, callback);
    }

    @Override
    public ResponsePromise serviceUnavailable(PromiseCallback<Response> callback)
    {
        return on(503, callback);
    }

    @Override
    public ResponsePromise error(PromiseCallback<Response> callback)
    {
        return clientError(callback).serverError(callback);
    }

    @Override
    public ResponsePromise notSuccessful(final PromiseCallback<Response> callback)
    {
        return informational(callback).redirection(callback).error(callback);
    }

    @Override
    public ResponsePromise others(final PromiseCallback<Response> callback)
    {
        done(new PromiseCallback<Response>()
        {
            @Override
            public void handle(Response response)
            {
                int status = response.getStatusCode();
                boolean inRanges = false;
                for (Range range : ranges)
                {
                    if (range.contains(status))
                    {
                        inRanges = true;
                        break;
                    }
                }
                if (!inRanges && !statuses.contains(status))
                {
                    callback.handle(response);
                }
            }
        });
        return this;
    }

    @Override
    public ResponsePromise done(PromiseCallback<Response> callback)
    {
        super.done(callback);
        return this;
    }

    @Override
    public ResponsePromise fail(PromiseCallback<Throwable> callback)
    {
        super.fail(callback);
        return this;
    }

    @Override
    public ResponsePromise then(FutureCallback<Response> callback)
    {
        super.then(callback);
        return this;
    }

    private class StatusSelector implements PromiseCallback<Response>
    {
        private final int statusCode;
        private final PromiseCallback<Response> callback;

        private StatusSelector(int statusCode, PromiseCallback<Response> callback)
        {
            statuses.add(statusCode);
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

    private class StatusRangeSelector implements PromiseCallback<Response>
    {
        private final Range range;
        private final PromiseCallback<Response> callback;

        private StatusRangeSelector(Range range, PromiseCallback<Response> callback)
        {
            this.range = range;
            ranges.add(range);
            this.callback = callback;
        }

        @Override
        public void handle(Response response)
        {
            if (range.contains(response.getStatusCode()))
            {
                callback.handle(response);
            }
        }
    }

    private static class Range
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
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
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
}
