package com.atlassian.plugin.connect.plugin.web.context;

import com.atlassian.plugin.connect.plugin.web.context.condition.InlineConditionVariableSubstitutor;

import java.util.Map;
import java.util.Optional;

public class InlineConditionVariableSubstitutorFake implements InlineConditionVariableSubstitutor {
    @Override
    public Optional<String> substitute(final String variable, final Map<String, ?> context) {
        return Optional.empty();
    }
}
