package com.atlassian.plugin.connect.spi.web.condition;

import java.util.Set;

public interface PageConditionsFactory
{
    Set<String> getConditionNames();

    ConditionClassResolver getPageConditions();
}
