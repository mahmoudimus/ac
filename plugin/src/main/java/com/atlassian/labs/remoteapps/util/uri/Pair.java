package com.atlassian.labs.remoteapps.util.uri;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A tuple of size 2.
 *
 * @param <A> type of the first value
 * @param <B> type of the second value
 */
public final class Pair<A, B>
{
    private final A first;
    private final B second;

    Pair(A first, B second)
    {
        this.first = checkNotNull(first, "first");
        this.second = checkNotNull(second, "second");
    }
    
    /**
     * @return the first value
     */
    public A first()
    {
        return first;
    }
    
    /**
     * @return the second value
     */
    public B second()
    {
        return second;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        final Pair<?, ?> pair = Pair.class.cast(o);

        return first.equals(pair.first) && second.equals(pair.second);
    }

    @Override
    public int hashCode()
    {
        return 31 * first.hashCode() + second.hashCode();
    }

    @Override
    public String toString()
    {
        return "(" + first + ", " + second + ")";
    }
    
    /**
     * Static factory method for creating pairs.
     * 
     * @param <A> type of the first value
     * @param <B> type of the second value
     * @param first the first value of the pair
     * @param second the second value of the pair
     * @return a new pair consisting of the first and second values
     */
    public static <A, B> Pair<A, B> pair(A first, B second)
    {
        return new Pair<A, B>(first, second);
    }
}
