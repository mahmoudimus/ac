package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.plugin.connect.modules.beans.builder.BaseModuleBeanBuilder;

import static com.atlassian.plugin.connect.modules.util.ConnectReflectionHelper.copyFieldsByNameAndType;

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
