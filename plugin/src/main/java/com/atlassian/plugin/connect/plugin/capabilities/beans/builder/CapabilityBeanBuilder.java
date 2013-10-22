package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import com.atlassian.plugin.connect.plugin.capabilities.beans.CapabilityBean;

/**
 * @since version
 */
public interface CapabilityBeanBuilder<B extends CapabilityBean>
{
    B build();
}
