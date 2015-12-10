package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.connect.modules.beans.ProjectPermissionCategory;
import com.atlassian.plugin.connect.modules.beans.ProjectPermissionModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class ProjectPermissionModuleBeanBuilder
        extends RequiredKeyBeanBuilder<ProjectPermissionModuleBeanBuilder, ProjectPermissionModuleBean>
{
    private I18nProperty description;
    private ProjectPermissionCategory category;
    private List<ConditionalBean> conditions;

    public ProjectPermissionModuleBeanBuilder()
    {
        this.description = I18nProperty.empty();
        this.category = ProjectPermissionCategory.OTHER;
        this.conditions = newArrayList();
    }

    public ProjectPermissionModuleBeanBuilder(ProjectPermissionModuleBean projectPermissionModuleBean)
    {
        super(projectPermissionModuleBean);
        this.description = projectPermissionModuleBean.getDescription();
        this.category = projectPermissionModuleBean.getCategory();
        this.conditions = newArrayList(projectPermissionModuleBean.getConditions());
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

    public ProjectPermissionModuleBeanBuilder withConditions(ConditionalBean... beans)
    {
        return withConditions(Arrays.asList(beans));
    }

    public ProjectPermissionModuleBeanBuilder withConditions(Collection<? extends ConditionalBean> beans)
    {
        if (beans != null) // not sure why this comes in as null sometimes
        {
            if (null == conditions)
            {
                conditions = newArrayList();
            }

            conditions.addAll(beans);
        }

        return this;

    }

    @Override
    public ProjectPermissionModuleBean build()
    {
        return new ProjectPermissionModuleBean(this);
    }
}
