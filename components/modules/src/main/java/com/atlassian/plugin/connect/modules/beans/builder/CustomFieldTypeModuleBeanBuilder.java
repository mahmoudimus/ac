package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.CFTArchetypeConfiguration;
import com.atlassian.plugin.connect.modules.beans.CustomFieldTypeModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;

public class CustomFieldTypeModuleBeanBuilder
        extends RequiredKeyBeanBuilder<CustomFieldTypeModuleBeanBuilder, CustomFieldTypeModuleBean>
{
    private CFTArchetypeConfiguration archetypeConfiguration;
    private I18nProperty description;

    public CustomFieldTypeModuleBeanBuilder()
    {
        this.archetypeConfiguration = new CFTArchetypeConfiguration();
    }

    public CustomFieldTypeModuleBeanBuilder(CustomFieldTypeModuleBean customFieldTypeModuleBean)
    {
        super(customFieldTypeModuleBean);
        this.archetypeConfiguration = customFieldTypeModuleBean.getArchetypeConfiguration();
        this.description = customFieldTypeModuleBean.getDescription();
    }

    public CustomFieldTypeModuleBeanBuilder withArchetypeConfiguration(CFTArchetypeConfiguration type)
    {
        this.archetypeConfiguration = type;
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
