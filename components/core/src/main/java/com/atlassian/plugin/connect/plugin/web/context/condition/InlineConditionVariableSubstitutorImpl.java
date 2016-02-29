package com.atlassian.plugin.connect.plugin.web.context.condition;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class InlineConditionVariableSubstitutorImpl implements InlineConditionVariableSubstitutor {
    private final InlineConditionParser parser;
    private final InlineConditionResolver resolver;

    @Autowired
    public InlineConditionVariableSubstitutorImpl(final InlineConditionParser inlineConditionParser, final InlineConditionResolver inlineConditionResolver) {
        this.parser = inlineConditionParser;
        this.resolver = inlineConditionResolver;
    }

    public Optional<String> substitute(String variable, Map<String, ?> context) {
        return parser.parse(variable)
                .flatMap(condition -> resolver.resolve(condition, new HashMap<>(context)))
                .map(Object::toString)
                .map(String::toLowerCase);
    }

}
