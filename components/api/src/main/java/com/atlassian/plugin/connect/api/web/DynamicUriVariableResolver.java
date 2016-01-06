package com.atlassian.plugin.connect.api.web;

import java.util.Map;
import java.util.Optional;

public interface DynamicUriVariableResolver
{
    Optional<String> resolve(String variable, Map<String, ?> context);
}
