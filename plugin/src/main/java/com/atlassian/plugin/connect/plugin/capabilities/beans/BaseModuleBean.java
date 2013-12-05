package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.BaseModuleBeanBuilder;

import static com.atlassian.plugin.connect.plugin.capabilities.util.ModuleBeanUtils.copyFieldsByNameAndType;

/**
 * @since 1.0
 */
public class BaseModuleBean implements ModuleBean
{
    protected BaseModuleBean()
    {
    }

    public BaseModuleBean(BaseModuleBeanBuilder builder)
    {
        copyFieldsByNameAndType(builder, this);
    }
}
