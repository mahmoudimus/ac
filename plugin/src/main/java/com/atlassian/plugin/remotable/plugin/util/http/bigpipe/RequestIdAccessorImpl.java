package com.atlassian.plugin.remotable.plugin.util.http.bigpipe;

import com.atlassian.plugin.remotable.api.service.http.bigpipe.RequestIdAccessor;
import com.atlassian.security.random.SecureRandomFactory;

import java.security.SecureRandom;

import static org.apache.commons.lang.Validate.notNull;

/**
 * Accesses the request id from the thread.
 */
final class RequestIdAccessorImpl implements RequestIdAccessor
{
    private static final ThreadLocal<String> id = new ThreadLocal<String>();
    private static final SecureRandom random = SecureRandomFactory.newInstance();

    @Override
    public String getRequestId()
    {
        String value = id.get();
        notNull(value);
        return value;
    }

    @Override
    public String resetRequestId()
    {
        String value = String.valueOf(Math.abs(random.nextLong()));
        id.set(value);
        return value;
    }
}
