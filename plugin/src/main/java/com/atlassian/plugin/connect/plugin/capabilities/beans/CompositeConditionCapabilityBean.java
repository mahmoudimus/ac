package com.atlassian.plugin.connect.plugin.capabilities.beans;

import java.util.List;

/**
 * @since version
 */
public class CompositeConditionCapabilityBean implements CapabilityWithConditionsBean
{

    @Override
    public List<SingleConditionCapabilityBean> getSingleConditions()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<CompositeConditionCapabilityBean> getCompositeConditions()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
