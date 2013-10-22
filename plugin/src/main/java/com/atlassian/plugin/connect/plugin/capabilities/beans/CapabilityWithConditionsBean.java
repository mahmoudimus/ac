package com.atlassian.plugin.connect.plugin.capabilities.beans;

import java.util.List;

import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.CompositeConditionBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.SingleConditionBean;

/**
 * @since version
 */
public interface CapabilityWithConditionsBean
{
    List<SingleConditionBean> getSingleConditions();
    
    List<CompositeConditionBean> getCompositeConditions();
}
