package com.atlassian.plugin.connect.spi.web.context;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * A plugin module descriptor interface for providers of context parameters for Atlassian Connect.
 *
 * A context parameter mapper defines a mapping from a specific type of object stored in the web fragment context to
 * an Atlassian Connect web fragment context parameter with a specific name and a string value. Each mapper is also
 * responsible for ensuring that the current user should have access to the parameter value being returned.
 *
 * @param <T> the type of object extracted from the web fragment context
 */
public interface TypeBasedConnectContextParameterMapper<T> extends ConnectContextParameterMapper<T>
{

    @Override
    default Optional<T> extractContextValue(Map<String, Object> context)
    {
        Class<T> contextValueClass = getContextValueClass();
        return Optional.ofNullable(context.get(getContextKey()))
                .filter(contextValueClass::isInstance)
                .map(contextValueClass::cast);
    }

    /**
     * The key of the web fragment context at which the mapped value can be found.
     *
     * @return the context key
     */
    String getContextKey();

    /**
     * Returns the type of the value extracted from the web fragment context.
     *
     * @return the context value class
     */
    Class<T> getContextValueClass();

    default Set<AdditionalValue<T, ?>> getAdditionalContextValues()
    {
        return Collections.emptySet();
    }

    static <T> void addContextEntry(TypeBasedConnectContextParameterMapper<T> parameterMapper, T contextValue, Map<String, Object> context)
    {
        context.put(parameterMapper.getContextKey(), contextValue);
    }

    static <T, U> AdditionalValue<T, U> additionalValue(Class<U> contextValueClass, Function<T, U> valueMapper)
    {
        return new AdditionalValue<T, U>()
        {
            @Override
            public Class<U> getContextValueClass()
            {
                return contextValueClass;
            }

            @Override
            public U getValue(T contextValue)
            {
                return valueMapper.apply(contextValue);
            }
        };
    }

    interface AdditionalValue<T, U>
    {

        Class<U> getContextValueClass();

        U getValue(T contextValue);
    }
}
