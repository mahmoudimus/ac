package com.atlassian.plugin.connect.modules.util;

import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionBean;
import com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean;

import java.util.List;

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
}
