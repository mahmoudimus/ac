package com.atlassian.plugin.connect.modules.util;

import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionBean;
import com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConditionUtils
{

    public static boolean isRemoteCondition(SingleConditionBean bean)
    {
        return isRemoteCondition(bean.getCondition());
    }

    public static boolean isRemoteCondition(String condition)
    {
        return (condition.startsWith("http") ||condition.startsWith("/"));
    }

    public static List<SingleConditionBean> getSingleConditionsRecursively(List<ConditionalBean> conditions)
    {
        return getSingleConditionsRecursively(conditions.stream()).collect(Collectors.toList());
    }

    public static Stream<SingleConditionBean> getSingleConditionsRecursively(Stream<ConditionalBean> conditionStream)
    {
        return conditionStream.flatMap(condition -> {
            if (SingleConditionBean.class.isAssignableFrom(condition.getClass()))
            {
                return Stream.of((SingleConditionBean) condition);
            }
            else
            {
                return getSingleConditionsRecursively(((CompositeConditionBean) condition).getConditions().stream());
            }
        });
    }
}
