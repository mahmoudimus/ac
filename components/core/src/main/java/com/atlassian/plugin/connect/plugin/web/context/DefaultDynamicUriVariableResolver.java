package com.atlassian.plugin.connect.plugin.web.context;

import java.util.Map;
import java.util.Optional;

import com.atlassian.plugin.connect.spi.web.context.DynamicUriVariableResolver;
import org.springframework.stereotype.Component;

/**
 * Default implementation. We always need at least one otherwise DI fails,
 * so this is needed for products which don't implement any other.
 */
@Component
public class DefaultDynamicUriVariableResolver implements DynamicUriVariableResolver
{
    @Override
    public Optional<String> resolve(final String addOnKey, final String variable, final Map<String, ?> context)
    {
        return Optional.empty();
    }
}
