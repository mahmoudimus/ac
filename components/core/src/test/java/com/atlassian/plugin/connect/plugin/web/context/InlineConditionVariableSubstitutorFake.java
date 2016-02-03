package com.atlassian.plugin.connect.plugin.web.context;

import java.util.Map;
import java.util.Optional;

import com.atlassian.plugin.connect.plugin.web.context.condition.InlineConditionVariableSubstitutor;

public class InlineConditionVariableSubstitutorFake implements InlineConditionVariableSubstitutor
{
    @Override
    public Optional<String> substitute(final String variable, final Map<String, ?> context)
    {
        return Optional.empty();
    }
}
