package com.atlassian.plugin.connect.spi.module.context;

import java.util.Map;

/**
 * Implementations of this interface are supposed to serialize parameters from P2 context to connect context.
 */
public interface ParameterSerializer<T>
{
    /**
     * Serializes the whitelisted parameters from a context.
     * @param t object to serialize.
     * @return map containing key under which parameters are stores and their value. For instance: project.id to 1.
     */
    Map<String, Object> serialize(T t);
}
