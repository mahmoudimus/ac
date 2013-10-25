package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import com.atlassian.plugin.connect.plugin.capabilities.beans.CapabilityBean;

/**
 * @since 1.0
 */
public interface CapabilityBeanBuilder<B extends CapabilityBean>
{
    B build();
}
