package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.CustomFieldBaseTypeConfiguration;
import com.atlassian.plugin.connect.modules.beans.CustomFieldTypeModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;

public class CustomFieldTypeModuleBeanBuilder
        extends RequiredKeyBeanBuilder<CustomFieldTypeModuleBeanBuilder, CustomFieldTypeModuleBean>
{
    private CustomFieldBaseTypeConfiguration baseTypeConfiguration;
    private I18nProperty description;

    public CustomFieldTypeModuleBeanBuilder withBaseTypeConfiguration(CustomFieldBaseTypeConfiguration type)
    {
        this.baseTypeConfiguration = type;
        return this;
    }

    public CustomFieldTypeModuleBeanBuilder withDescription(I18nProperty description)
    {
        this.description = description;
        return this;
    }

    @Override
    public CustomFieldTypeModuleBean build()
    {
        return new CustomFieldTypeModuleBean(this);
    }

}
