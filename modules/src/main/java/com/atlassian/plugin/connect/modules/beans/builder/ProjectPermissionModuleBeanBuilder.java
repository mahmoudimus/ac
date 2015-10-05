package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.GlobalPermissionModuleBean;
import com.atlassian.plugin.connect.modules.beans.ProjectPermissionCategory;
import com.atlassian.plugin.connect.modules.beans.ProjectPermissionModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;

public class ProjectPermissionModuleBeanBuilder extends BeanWithKeyParamsAndConditionsBuilder<ProjectPermissionModuleBeanBuilder, ProjectPermissionModuleBean>
{
    private I18nProperty description;
    private ProjectPermissionCategory category;

    public ProjectPermissionModuleBeanBuilder()
    {
        this.description = I18nProperty.empty();
        this.category = ProjectPermissionCategory.OTHER;
    }

    public ProjectPermissionModuleBeanBuilder(ProjectPermissionModuleBean projectPermissionModuleBean)
    {
        super(projectPermissionModuleBean);
        this.description = projectPermissionModuleBean.getDescription();
        this.category = projectPermissionModuleBean.getCategory();
    }

    public ProjectPermissionModuleBeanBuilder withDescription(I18nProperty description)
    {
        this.description = description;
        return this;
    }

    public ProjectPermissionModuleBeanBuilder withCategory(ProjectPermissionCategory projectPermissionCategory)
    {
        this.category = projectPermissionCategory;
        return this;
    }

    @Override
    public ProjectPermissionModuleBean build()
    {
        return new ProjectPermissionModuleBean(this);
    }
}
