package com.atlassian.plugin.connect.spi.web.context;

import java.util.Map;
import java.util.Optional;

public interface DynamicUriVariableResolver
{
    /**
     * Resolve URI variable dynamically, which means not necessarily based on context but
     * possibly on the system state and additionally the add-on key.
     *
     * @param addOnKey key of the add-on the URL variable comes from. May be empty.
     * @param variable variable to resolve
     * @param context context map
     *
     * @return the variable value if it's possible to resolve it, empty otherwise
     */
    Optional<String> resolve(final String addOnKey, String variable, Map<String, ?> context);
}
