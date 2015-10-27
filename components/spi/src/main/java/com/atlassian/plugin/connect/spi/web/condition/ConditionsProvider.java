package com.atlassian.plugin.connect.spi.web.condition;

public interface ConditionsProvider
{
    ConditionClassResolver getConditions();
}
