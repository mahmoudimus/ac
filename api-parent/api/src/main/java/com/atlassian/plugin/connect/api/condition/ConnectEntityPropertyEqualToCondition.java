package com.atlassian.plugin.connect.api.condition;

import com.atlassian.plugin.web.Condition;
import com.google.common.base.Predicate;

import java.util.Map;

public interface ConnectEntityPropertyEqualToCondition extends Condition
{
    String ENTITY_PROPERTY_EQUAL_TO = "entity_property_equal_to";

    Predicate<Map<String, String>> RULE_PREDICATE = new Predicate<Map<String, String>>()
    {
        @Override
        public boolean apply(final Map<String, String> parameters)
        {
            return "addon".equals(parameters.get("entity"));
        }
    };
}
