package com.atlassian.plugin.connect.plugin.capabilities.beans.nested;

import java.util.List;

import com.atlassian.plugin.connect.plugin.capabilities.beans.CapabilityWithConditionsBean;

/**
 * @since version
 */
public class CompositeConditionBean implements CapabilityWithConditionsBean
{

    @Override
    public List<SingleConditionBean> getSingleConditions()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<CompositeConditionBean> getCompositeConditions()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
