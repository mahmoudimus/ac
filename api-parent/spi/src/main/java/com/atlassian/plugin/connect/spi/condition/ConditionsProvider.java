package com.atlassian.plugin.connect.spi.condition;

import com.atlassian.plugin.connect.spi.product.ConditionClassResolver;

public interface ConditionsProvider
{
    ConditionClassResolver getConditions();
}
