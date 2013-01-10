package com.atlassian.plugin.remotable.host.common.service.http.bigpipe;

import com.atlassian.security.random.SecureRandomFactory;

import java.security.SecureRandom;

import static org.apache.commons.lang.Validate.notNull;

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
        notNull(value);
        return value;
    }

    public String resetRequestId()
    {
        String value = Long.toHexString(Math.abs(random.nextLong()));
        id.set(value);
        return value;
    }
}
