package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.plugin.connect.modules.beans.builder.CustomFieldBaseTypeConfigurationBeanBuilder;

public class CustomFieldBaseTypeConfiguration extends BaseModuleBean
{
    @Required
    private CustomFieldBaseType baseType;

    public CustomFieldBaseTypeConfiguration()
    {
        this.baseType = CustomFieldBaseType.TEXT;
    }

    public CustomFieldBaseTypeConfiguration(CustomFieldBaseTypeConfigurationBeanBuilder builder)
    {
        super(builder);
    }

    public CustomFieldBaseType getBaseType()
    {
        return baseType;
    }

    public void setType(final CustomFieldBaseType baseType)
    {
        this.baseType = baseType;
    }

    public static CustomFieldBaseTypeConfigurationBeanBuilder newBuilder()
    {
        return new CustomFieldBaseTypeConfigurationBeanBuilder();
    }
}
