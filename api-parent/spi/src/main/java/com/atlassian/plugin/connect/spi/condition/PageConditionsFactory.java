package com.atlassian.plugin.connect.spi.condition;

import com.atlassian.plugin.connect.spi.product.ConditionClassResolver;

import java.util.Set;

public interface PageConditionsFactory
{
    Set<String> getConditionNames();

    ConditionClassResolver getPageConditions();
}
