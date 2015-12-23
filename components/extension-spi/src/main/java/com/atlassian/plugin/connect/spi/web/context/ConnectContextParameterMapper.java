package com.atlassian.plugin.connect.spi.web.context;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
     * Extracts a value of the expected type from the web fragment context.
     * @param context the web fragment context
     * @return the context value, if found
     */
    Optional<T> extractContextValue(Map<String, Object> context);

    /**
     * The set of context parameters derived from the context value.
     *
     * @return the context parameters
     */
    Set<Parameter<T>> getParameters();

    /**
     * A context parameter
     *
     * @param <T> the type of object extracted from the web fragment context
     */
    interface Parameter<T>
    {

        /**
         * Returns if the current user should have access to the parameter value inferred from the context value.
         *
         * @param contextValue the value extracted from the web fragment context
         * @return true if access restrictions permit the parameter value being returned to the current user
         */
        boolean isAccessibleByCurrentUser(T contextValue);

        /**
         * Returns if the current user should have access to the parameter value.
         *
         * @param value the parameter value
         * @return true if access restrictions permit the parameter value being returned to the current user
         */
        boolean isValueAccessibleByCurrentUser(String value);

        /**
         * The constant key of the context parameter.
         *
         * @return the parameter key
         */
        String getKey();

        /**
         * The value of the context parameter.
         *
         * @param contextValue the value extracted from the web fragment context
         * @return the parameter value
         */
        String getValue(T contextValue);
    }
}
