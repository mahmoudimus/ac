package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.CommonSchemaAttributes;
import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.StringSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.builder.ProjectPermissionModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.google.common.base.Objects;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Add the project permission to the project permission list in the JIRA's setting page.
 * Pluggable project permissions can not be used in conditions yet.
 *
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
     * Category of the project permission. On it depends in which section permission will be displayed.
     * Currently JIRA support those categories:
     * * `projects`
     * * `issues`
     * * `voters.and.watchers`
     * * `comments`
     * * `attachments`
     * * `time.tracking`
     * * `other`
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