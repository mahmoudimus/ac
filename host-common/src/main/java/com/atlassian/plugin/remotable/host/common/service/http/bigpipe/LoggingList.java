package com.atlassian.plugin.remotable.host.common.service.http.bigpipe;

import com.google.common.collect.ForwardingList;
import org.slf4j.Logger;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public final class LoggingList<E> extends ForwardingList<E>
{
    private final Logger logger;
    private final String name;
    private final List<E> delegate;

    public LoggingList(Logger logger, String name, List<E> delegate)
    {
        this.logger = checkNotNull(logger);
        this.name = checkNotNull(name);
        this.delegate = checkNotNull(delegate);
    }

    @Override
    protected List<E> delegate()
    {
        return delegate;
    }

    @Override
    public boolean add(E element)
    {
        logger.debug("Adding to {} list: {}", name, element);
        return super.add(element);
    }

    @Override
    public boolean remove(Object element)
    {
        logger.debug("Removing from {} list: {}", name, element);
        return super.remove(element);
    }

    @Override
    public void clear()
    {
        logger.debug("Clearing '{}' list.", name);
        super.clear();
    }
}
