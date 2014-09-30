package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.ObjectSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.builder.BaseModuleBeanBuilder;

@ObjectSchemaAttributes(additionalProperties = false)
public abstract class BaseModuleList extends BaseModuleBean
{
    public BaseModuleList()
    {
    }

    public BaseModuleList(BaseModuleBeanBuilder builder)
    {
        super(builder);
    }
}