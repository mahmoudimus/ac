package com.atlassian.plugin.connect.plugin.capabilities.util;

import java.util.List;

import com.atlassian.plugin.connect.plugin.capabilities.beans.ConditionalBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.CompositeConditionBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.SingleConditionBean;

import org.dom4j.dom.DOMElement;

public class ConditionUtils
{
    public static boolean containsRemoteCondition(List<ConditionalBean> conditions)
    {
        return findRemoteConditions(conditions);
    }

    private static boolean findRemoteConditions(List<ConditionalBean> beans)
    {
        boolean foundRemote = false;
        
        for(ConditionalBean bean : beans)
        {
            if(SingleConditionBean.class.isAssignableFrom(bean.getClass()))
            {
                if(isRemoteCondition((SingleConditionBean)bean))
                {
                    foundRemote = true;
                    break;
                }
            }
            else if(CompositeConditionBean.class.isAssignableFrom(bean.getClass()))
            {
                CompositeConditionBean ccb = (CompositeConditionBean) bean;

                if(findRemoteConditions(ccb.getConditions()))
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
        return (bean.getCondition().startsWith("http") || bean.getCondition().startsWith("/"));
    }
}
