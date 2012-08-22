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
    public ResponsePromise informational(PromiseCallback<Response> callback)
    {
        done(new StatusRangeSieve(new Range(0, 200), callback));
        return this;
    }

    @Override
    public ResponsePromise successful(PromiseCallback<Response> callback)
    {
        done(new StatusRangeSieve(new Range(200, 300), callback));
        return this;
    }

    @Override
    public ResponsePromise ok(PromiseCallback<Response> callback)
    {
        done(new StatusSieve(200, callback));
        return this;
    }

    @Override
    public ResponsePromise created(PromiseCallback<Response> callback)
    {
        done(new StatusSieve(201, callback));
        return this;
    }

    @Override
    public ResponsePromise noContent(PromiseCallback<Response> callback)
    {
        done(new StatusSieve(204, callback));
        return this;
    }

    @Override
    public ResponsePromise redirection(PromiseCallback<Response> callback)
    {
        done(new StatusRangeSieve(new Range(300, 400), callback));
        return this;
    }

    @Override
    public ResponsePromise seeOther(PromiseCallback<Response> callback)
    {
        done(new StatusSieve(303, callback));
        return this;
    }

    @Override
    public ResponsePromise notModified(PromiseCallback<Response> callback)
    {
        done(new StatusSieve(304, callback));
        return this;
    }

    @Override
    public ResponsePromise clientError(PromiseCallback<Response> callback)
    {
        done(new StatusRangeSieve(new Range(400, 500), callback));
        return this;
    }

    @Override
    public ResponsePromise badRequest(PromiseCallback<Response> callback)
    {
        done(new StatusSieve(400, callback));
        return this;
    }

    @Override
    public ResponsePromise unauthorized(PromiseCallback<Response> callback)
    {
        done(new StatusSieve(401, callback));
        return this;
    }

    @Override
    public ResponsePromise forbidden(PromiseCallback<Response> callback)
    {
        done(new StatusSieve(403, callback));
        return this;
    }

    @Override
    public ResponsePromise conflict(PromiseCallback<Response> callback)
    {
        done(new StatusSieve(409, callback));
        return this;
    }

    @Override
    public ResponsePromise serverError(PromiseCallback<Response> callback)
    {
        done(new StatusRangeSieve(new Range(500, 600), callback));
        return this;
    }

    @Override
    public ResponsePromise internalServerError(PromiseCallback<Response> callback)
    {
        done(new StatusSieve(500, callback));
        return this;
    }

    @Override
    public ResponsePromise serviceUnavailable(PromiseCallback<Response> callback)
    {
        done(new StatusSieve(503, callback));
        return this;
    }

    @Override
    public ResponsePromise error(PromiseCallback<Response> callback)
    {
        done(new StatusRangeSieve(new Range(400, 600), callback));
        return this;
    }

    @Override
    public ResponsePromise notSuccessful(final PromiseCallback<Response> callback)
    {
        done(new PromiseCallback<Response>()
        {
            @Override
            public void handle(Response response)
            {
                int status = response.getStatusCode();
                if (status < 200 || status >= 300)
                {
                    callback.handle(response);
                }
            }
        });
        return this;
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
                boolean inRange = false;
                for (Range range : ranges)
                {
                    if (range.contains(status))
                    {
                        inRange = true;
                        break;
                    }
                }
                if (!inRange && !statuses.contains(status))
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

    private class StatusSieve implements PromiseCallback<Response>
    {
        private final int statusCode;
        private final PromiseCallback<Response> callback;

        private StatusSieve(int statusCode, PromiseCallback<Response> callback)
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

    private class StatusRangeSieve implements PromiseCallback<Response>
    {
        private final Range range;
        private final PromiseCallback<Response> callback;

        private StatusRangeSieve(Range range, PromiseCallback<Response> callback)
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
