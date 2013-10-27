package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.BaseCapabilityBeanBuilder;

import static com.atlassian.plugin.connect.plugin.capabilities.util.CapabilityBeanUtils.copyFieldsByNameAndType;

/**
 * @since 1.0
 */
public class BaseCapabilityBean implements CapabilityBean
{

    protected BaseCapabilityBean()
    {
    }

    public BaseCapabilityBean(BaseCapabilityBeanBuilder builder)
    {
        copyFieldsByNameAndType(builder, this);
    }
}
