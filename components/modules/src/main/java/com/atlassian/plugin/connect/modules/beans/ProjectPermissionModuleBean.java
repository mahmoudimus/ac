package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.CommonSchemaAttributes;
import com.atlassian.json.schema.annotation.Required;
import com.atlassian.plugin.connect.modules.beans.builder.ProjectPermissionModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;

/**
 * Create the project permission.
 *
 * Project permissions are useful if you need to manage permissions for operations performed on objects
 * related with project like issues, comments, versions or your add-on's entities related with projects.

 * User's permissions can be checked using <a href="https://docs.atlassian.com/jira/REST/latest/#api/2/-getPermissions">my permission resource</a>.
 * Project permissions are not supported in conditions yet.
 *
 * You can define condition for project permissions. I may sense if permission that depends on global JIRA settings like time-tracking, voting or sub-tasks.
 *
 * #### Example
 * @exampleJson {@link ConnectJsonExamples#PROJECT_PERMISSION_EXAMPLE}
 * @schemaTitle Project Permission
 * @since 1.1
 */
public class ProjectPermissionModuleBean extends BeanWithKeyAndParamsAndConditions
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

    public ProjectPermissionModuleBean()
    {
        super();
        this.description = I18nProperty.empty();
        this.category = ProjectPermissionCategory.OTHER;
    }

    public ProjectPermissionModuleBean(ProjectPermissionModuleBeanBuilder builder)
    {
        super(builder);
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
}