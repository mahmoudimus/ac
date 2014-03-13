package com.atlassian.plugin.connect.modules.util;

import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionBean;
import com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean;

import java.util.List;

public class ConditionUtils
{
    public static boolean containsRemoteCondition(List<ConditionalBean> conditions)
    {
        return findRemoteConditions(conditions);
    }

    private static boolean findRemoteConditions(List<ConditionalBean> beans)
    {
        boolean foundRemote = false;

        for (ConditionalBean bean : beans)
        {
            if (SingleConditionBean.class.isAssignableFrom(bean.getClass()))
            {
                if (isRemoteCondition((SingleConditionBean) bean))
                {
                    foundRemote = true;
                    break;
                }
            }
            else if (CompositeConditionBean.class.isAssignableFrom(bean.getClass()))
            {
                CompositeConditionBean ccb = (CompositeConditionBean) bean;

                if (findRemoteConditions(ccb.getConditions()))
                {
                    foundRemote = true;
                    break;
                }
            }
        }

        return foundRemote;
    }

    public static boolean isRemoteCondition(SingleConditionBean bean)
    {
        return isRemoteCondition(bean.getCondition());
    }

    public static boolean isRemoteCondition(String condition)
    {
        return (condition.startsWith("http") ||condition.startsWith("/"));
    }
}
