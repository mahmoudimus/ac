package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.CommonSchemaAttributes;
import com.atlassian.json.schema.annotation.Required;
import com.atlassian.plugin.connect.modules.beans.builder.BeanWithKeyAndParamsBuilder;
import com.atlassian.plugin.connect.modules.beans.builder.ProjectPermissionModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Create the project permission.
 *
 * Project permissions are useful if you need to manage permissions for operations performed on objects
 * related with project like issues, comments, versions or your add-on's entities related with projects.

 * User's permissions can be checked using [/mypermissions](https://docs.atlassian.com/jira/REST/latest/#api/2/-getPermissions).
 * __Note:__ Project permissions added using this module are not supported for use with the `has_project_permission` condition.
 *
 * You can define condition for project permissions. I may sense if permission that depends on global JIRA settings like time-tracking, voting or sub-tasks.
 *
 * #### Example
 * @exampleJson {@link ConnectJsonExamples#PROJECT_PERMISSION_EXAMPLE}
 * @schemaTitle Project Permission
 * @since 1.1
 */
public class ProjectPermissionModuleBean extends RequiredKeyBean
{
    /**
     * Description of the project permission. It will be displayed under the permission's name.
     */
    @Required
    private I18nProperty description;

    /**
     * The category of the project permission. This determines in which section the permission will be displayed.
     */
    @CommonSchemaAttributes (defaultValue = "other")
    private ProjectPermissionCategory category;

    /**
     * <a href="../../concepts/conditions.html">Conditions</a> can be added to display only when all the given conditions are true.
     */
    private List<ConditionalBean> conditions;

    public ProjectPermissionModuleBean()
    {
        super();
        this.description = I18nProperty.empty();
        this.category = ProjectPermissionCategory.OTHER;
        this.conditions = newArrayList();
    }

    public ProjectPermissionModuleBean(ProjectPermissionModuleBeanBuilder builder)
    {
        super(builder);

        if (null == conditions)
        {
            this.conditions = newArrayList();
        }
    }

    public static ProjectPermissionModuleBeanBuilder newProjectPermissionModuleBean()
    {
        return new ProjectPermissionModuleBeanBuilder();
    }

    public I18nProperty getDescription()
    {
        return description;
    }

    public ProjectPermissionCategory getCategory()
    {
        return category;
    }

    public List<ConditionalBean> getConditions()
    {
        return conditions;
    }

    @Override
    public boolean equals(final Object otherObj)
    {
        if (this == otherObj) { return true; }

        if (otherObj == null || getClass() != otherObj.getClass()) { return false; }

        final ProjectPermissionModuleBean that = (ProjectPermissionModuleBean) otherObj;

        return new EqualsBuilder()
                .appendSuper(super.equals(otherObj))
                .append(description, that.description)
                .append(category, that.category)
                .append(conditions, that.conditions)
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(description)
                .append(category)
                .append(conditions)
                .toHashCode();
    }
}