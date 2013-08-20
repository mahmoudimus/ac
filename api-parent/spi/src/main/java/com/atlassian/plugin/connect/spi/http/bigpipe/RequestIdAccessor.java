package com.atlassian.plugin.connect.spi.http.bigpipe;

import java.security.SecureRandom;

import com.atlassian.security.random.SecureRandomFactory;

/**
 * Accesses the request id from the thread.
 */
final class RequestIdAccessor
{
    private static final ThreadLocal<String> id = new ThreadLocal<String>();
    private static final SecureRandom random = SecureRandomFactory.newInstance();

    public String getRequestId()
    {
        String value = id.get();
        if (value == null)
        {
            value = Long.toHexString(Math.abs(random.nextLong()));
            id.set(value);
        }
        return value;
    }

    public void resetRequestId()
    {
        id.set(null);
    }
}
