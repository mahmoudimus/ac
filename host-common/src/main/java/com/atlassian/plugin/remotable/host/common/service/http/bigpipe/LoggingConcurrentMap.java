package com.atlassian.plugin.remotable.host.common.service.http.bigpipe;

import com.google.common.collect.ForwardingConcurrentMap;
import org.slf4j.Logger;

import java.util.concurrent.ConcurrentMap;

import static com.google.common.base.Preconditions.checkNotNull;

final class LoggingConcurrentMap extends ForwardingConcurrentMap<String, DefaultBigPipeManager.BigPipeImpl>
{
    private final Logger logger;
    private final ConcurrentMap<String, DefaultBigPipeManager.BigPipeImpl> delegate;

    LoggingConcurrentMap(Logger logger, ConcurrentMap<String, DefaultBigPipeManager.BigPipeImpl> delegate)
    {
        this.logger = checkNotNull(logger);
        this.delegate = checkNotNull(delegate);
    }

    @Override
    protected ConcurrentMap<String, DefaultBigPipeManager.BigPipeImpl> delegate()
    {
        return delegate;
    }

    @Override
    public DefaultBigPipeManager.BigPipeImpl get(Object key)
    {
        final DefaultBigPipeManager.BigPipeImpl bigPipe = super.get(key);
        logger.debug("Getting big pipe for request '{}'. It's value is {}", key, bigPipe);
        return bigPipe;
    }

    @Override
    public DefaultBigPipeManager.BigPipeImpl remove(Object key)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Removing big pipe for request '{}', which is {}in the map.", key, containsKey(key) ? "" : "NOT");
        }
        return super.remove(key);
    }

    @Override
    public DefaultBigPipeManager.BigPipeImpl putIfAbsent(String key, DefaultBigPipeManager.BigPipeImpl value)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Adding big pipe for request '{}' if not already there. It's value is {}", key, value);
        }
        return super.putIfAbsent(key, value);
    }
}
