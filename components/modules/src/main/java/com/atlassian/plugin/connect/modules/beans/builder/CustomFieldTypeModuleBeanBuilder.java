package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.CustomFieldTypeModuleBean;
import com.atlassian.plugin.connect.modules.beans.DashboardItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;

public class CustomFieldTypeModuleBeanBuilder
        extends RequiredKeyBeanBuilder<CustomFieldTypeModuleBeanBuilder, CustomFieldTypeModuleBean>
{
    private String type;

    public CustomFieldTypeModuleBeanBuilder()
    {
        this.type = "";
    }

    public CustomFieldTypeModuleBeanBuilder(CustomFieldTypeModuleBean customFieldTypeModuleBean)
    {
        super(customFieldTypeModuleBean);
        this.type = customFieldTypeModuleBean.getType();
    }

    public CustomFieldTypeModuleBeanBuilder withType(String type)
    {
        this.type = type;
        return this;
    }

    @Override
    public CustomFieldTypeModuleBean build()
    {
        return new CustomFieldTypeModuleBean(this);
    }

}
