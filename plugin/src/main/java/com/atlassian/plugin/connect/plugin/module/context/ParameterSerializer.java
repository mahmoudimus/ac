package com.atlassian.plugin.connect.plugin.module.context;

import java.util.Map;

/**
 * Implementations of this interface are supposed to serialize parameters from P2 context to connect context.
 */
// TODO: Temporarily extend ParameterDeserializer. Wanted to keep the two interfaces but this could lead to a painful refactor
public interface ParameterSerializer<T> extends ParameterDeserializer<T>
{
    /**
     * Serializes the whitelisted parameters from a context.
     * @param t object to serialize.
     * @return map containing key under which parameters are stores and their value. For instance: project.id -> 1.
     */
    Map<String, Object> serialize(T t);
}
