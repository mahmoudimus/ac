package com.atlassian.plugin.connect.plugin.condition;

import com.atlassian.plugin.connect.jira.condition.ConnectEntityPropertyEqualToCondition;
import com.atlassian.plugin.connect.spi.product.ConditionClassResolver;

public final class CrossProductConditions
{

    private static final ConditionClassResolver CONDITIONS = ConditionClassResolver.builder()
            .with(PageConditions.getConditionMap())
            .rule(ConnectEntityPropertyEqualToCondition.ENTITY_PROPERTY_EQUAL_TO, ConnectEntityPropertyEqualToCondition.RULE_PREDICATE, ConnectEntityPropertyEqualToCondition.class)
            .build();

    public static ConditionClassResolver getConditions()
    {
        return CONDITIONS;
    }
}
