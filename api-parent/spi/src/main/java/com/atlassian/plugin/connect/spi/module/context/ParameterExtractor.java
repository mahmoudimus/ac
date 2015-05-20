package com.atlassian.plugin.connect.spi.module.context;

import com.google.common.base.Optional;

/**
 * Implementations of this interfaces are supposed to find context objects in module-specific contexts.
 * @param <C> type of the context.
 * @param <P> type of the object retrieved from context.
 */
public interface ParameterExtractor<C, P>
{
    /**
     * Extracts the object from context.
     * @param context module context.
     * @return extracted o
     */
    Optional<P> extract(C context);

    ParameterSerializer<P> serializer();
}
