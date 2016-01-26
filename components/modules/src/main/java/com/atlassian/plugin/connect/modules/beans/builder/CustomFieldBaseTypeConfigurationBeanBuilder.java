package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.CustomFieldBaseType;
import com.atlassian.plugin.connect.modules.beans.CustomFieldBaseTypeConfiguration;

public class CustomFieldBaseTypeConfigurationBeanBuilder
        extends BaseModuleBeanBuilder<CustomFieldBaseTypeConfigurationBeanBuilder, CustomFieldBaseTypeConfiguration>
{
    private CustomFieldBaseType baseType;

    public CustomFieldBaseTypeConfigurationBeanBuilder withArchetype(CustomFieldBaseType type)
    {
        this.baseType = type;
        return this;
    }

    @Override
    public CustomFieldBaseTypeConfiguration build()
    {
        return new CustomFieldBaseTypeConfiguration(this);
    }
}
