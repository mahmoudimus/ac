package com.atlassian.labs.remoteapps.util.http.bigpipe;

import com.atlassian.security.random.SecureRandomFactory;

import java.security.SecureRandom;

import static org.apache.commons.lang.Validate.notNull;

/**
 * Accesses the request id from the thread.
 */
public class RequestIdAccessor
{
    private static final ThreadLocal<String> id = new ThreadLocal<String>();
    private static final SecureRandom random = SecureRandomFactory.newInstance();

    public static String getRequestId()
    {
        String value = id.get();
        notNull(value);
        return value;
    }

    public static String resetRequestId()
    {
        String value = String.valueOf(Math.abs(random.nextLong()));
        id.set(value);
        return value;
    }
}
