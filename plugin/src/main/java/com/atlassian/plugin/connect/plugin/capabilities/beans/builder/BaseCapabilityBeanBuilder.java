package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import com.atlassian.plugin.connect.plugin.capabilities.beans.CapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.BaseCapabilityBean;

/**
 * @since 1.0
 */
public abstract class BaseCapabilityBeanBuilder<T extends BaseCapabilityBeanBuilder, B extends BaseCapabilityBean> implements CapabilityBeanBuilder<B>
{

    public BaseCapabilityBeanBuilder()
    {
    }

    //just here for convienience
    public BaseCapabilityBeanBuilder(CapabilityBean defaultBean)
    {
    }

    @Override
    public B build()
    {
        return (B) new BaseCapabilityBean(this);
    }
}