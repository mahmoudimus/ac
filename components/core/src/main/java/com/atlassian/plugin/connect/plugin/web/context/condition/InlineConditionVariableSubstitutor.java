package com.atlassian.plugin.connect.plugin.web.context.condition;

import java.util.Map;
import java.util.Optional;

public interface InlineConditionVariableSubstitutor {
    Optional<String> substitute(String variable, Map<String, ?> context);
}
