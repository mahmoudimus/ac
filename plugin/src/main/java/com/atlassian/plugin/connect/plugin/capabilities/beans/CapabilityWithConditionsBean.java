package com.atlassian.plugin.connect.plugin.capabilities.beans;

import java.util.List;

/**
 * @since version
 */
public interface CapabilityWithConditionsBean
{
    List<SingleConditionCapabilityBean> getSingleConditions();
    
    List<CompositeConditionCapabilityBean> getCompositeConditions();
}
