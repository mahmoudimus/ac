package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.EntityPropertyModuleBean;
import com.atlassian.plugin.connect.modules.beans.GlobalPermissionModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;

public class GlobalPermissionModuleBeanBuilder extends RequiredKeyBeanBuilder<GlobalPermissionModuleBeanBuilder, GlobalPermissionModuleBean>
{
    private I18nProperty description;
    private Boolean anonymusAllowed;

    public GlobalPermissionModuleBeanBuilder()
    {
        this.description = I18nProperty.empty();
        this.anonymusAllowed = true;
    }

    public GlobalPermissionModuleBeanBuilder(GlobalPermissionModuleBean globalPermissionModuleBean)
    {
        super(globalPermissionModuleBean);
        this.description = globalPermissionModuleBean.getDescription();
        this.anonymusAllowed = globalPermissionModuleBean.getAnonymousAllowed();
    }

    public GlobalPermissionModuleBeanBuilder withDescription(I18nProperty description)
    {
        this.description = description;
        return this;
    }

    public GlobalPermissionModuleBeanBuilder withAnonymusAllowed(Boolean anonymusAllowed)
    {
        this.anonymusAllowed = anonymusAllowed;
        return this;
    }

    @Override
    public GlobalPermissionModuleBean build()
    {
        return new GlobalPermissionModuleBean(this);
    }
}
