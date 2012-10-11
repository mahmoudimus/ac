package com.atlassian.plugin.remotable.api;

import com.atlassian.util.concurrent.Effect;
import com.atlassian.util.concurrent.Promise;

public final class Deferreds
{
    private Deferreds()
    {
    }

    /**
     * Creates a new, resolved promise for the specified concrete value.
     *
     * @param value The value for which a promise should be created
     * @return The new promise
     */
    public static <V> Promise<V> resolved(V value)
    {
        return Deferred.<V>create().resolve(value).promise();
    }

    public static Effect<Throwable> reject(final Deferred<?> deferred)
    {
        return new Effect<Throwable>()
        {
            @Override
            public void apply(Throwable throwable)
            {
                deferred.reject(throwable);
            }
        };
    }
}
