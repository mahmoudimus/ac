package com.atlassian.plugin.connect.spi.web.context;

import java.util.Map;
import java.util.Optional;

/**
 * A plugin module descriptor interface for providers of context parameters for Atlassian Connect.
 *
 * A context parameter mapper defines a mapping from a specific type of object stored in the web fragment context to
 * an Atlassian Connect web fragment context parameter with a specific name and a string value. Each mapper is also
 * responsible for ensuring that the current user should have access to the parameter value being returned.
 *
 * @param <T> the type of object extracted from the web fragment context
 */
public interface ConnectContextParameterMapper<T>
{

    /**
     * Extracts a value from the given web fragment context to be used for obtaining the value of the context parameter.
     *
     * @param context the web fragment context
     * @return the extracted value
     */
    Optional<T> extractContextValue(Map<String, Object> context);

    /**
     * Returns if the current user should have access to the parameter value to be returned by this mapper (not to the
     * context value itself).
     *
     * @param contextValue the value extracted from the web fragment context
     * @return true if access restrictions permit the parameter value being returned to the current user
     */
    boolean isParameterValueAccessibleByCurrentUser(T contextValue);

    /**
     * The constant key of the context parameter.
     *
     * @return the parameter key
     */
    String getParameterKey();

    /**
     * The value of the context parameter.
     *
     * @param contextValue the value extracted from the web fragment context
     * @return the parameter value
     */
    String getParameterValue(T contextValue);
}
