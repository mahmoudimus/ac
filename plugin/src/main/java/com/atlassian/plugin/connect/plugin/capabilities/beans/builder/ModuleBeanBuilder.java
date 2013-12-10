package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import com.atlassian.plugin.connect.plugin.capabilities.beans.ModuleBean;

/**
 * @since 1.0
 */
public interface ModuleBeanBuilder<B extends ModuleBean>
{
    B build();
}
