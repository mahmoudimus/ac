package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import com.atlassian.plugin.connect.plugin.capabilities.beans.ModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.BaseModuleBean;

/**
 * @since 1.0
 */
public abstract class BaseModuleBeanBuilder<T extends BaseModuleBeanBuilder, B extends BaseModuleBean> implements ModuleBeanBuilder<B>
{

    public BaseModuleBeanBuilder()
    {
    }

    //just here for convienience
    public BaseModuleBeanBuilder(ModuleBean defaultBean)
    {
    }

    @Override
    public B build()
    {
        return (B) new BaseModuleBean(this);
    }
}
