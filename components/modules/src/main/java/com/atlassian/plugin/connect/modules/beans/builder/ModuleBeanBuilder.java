package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.ModuleBean;

/**
 * @since 1.0
 */
public interface ModuleBeanBuilder<B extends ModuleBean> {
    B build();
}
