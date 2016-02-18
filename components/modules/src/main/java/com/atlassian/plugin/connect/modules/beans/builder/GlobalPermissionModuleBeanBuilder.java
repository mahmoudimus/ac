package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.GlobalPermissionModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;

public class GlobalPermissionModuleBeanBuilder
        extends NamedBeanBuilder<GlobalPermissionModuleBeanBuilder, GlobalPermissionModuleBean>
{
    private I18nProperty description;
    private Boolean anonymousAllowed;

    public GlobalPermissionModuleBeanBuilder()
    {
        this.description = I18nProperty.empty();
        this.anonymousAllowed = true;
    }

    public GlobalPermissionModuleBeanBuilder(GlobalPermissionModuleBean globalPermissionModuleBean)
    {
        super(globalPermissionModuleBean);
        this.description = globalPermissionModuleBean.getDescription();
        this.anonymousAllowed = globalPermissionModuleBean.getAnonymousAllowed();
    }

    public GlobalPermissionModuleBeanBuilder withDescription(I18nProperty description)
    {
        this.description = description;
        return this;
    }

    public GlobalPermissionModuleBeanBuilder withAnonymousAllowed(Boolean anonymousAllowed)
    {
        this.anonymousAllowed = anonymousAllowed;
        return this;
    }

    @Override
    public GlobalPermissionModuleBean build()
    {
        return new GlobalPermissionModuleBean(this);
    }
}
