package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.plugin.connect.modules.beans.builder.IssueFieldModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;

/**
 * Custom field type module allows the add-on to add new custom field types to JIRA.
 *
 * #### Example
 * @schemaTitle Custom Field Type
 * @exampleJson {@link com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#ISSUE_FIELD_EXAMPLE}
 * @since 1.0
 */
public class ConnectFieldModuleBean extends RequiredKeyBean
{
    /**
     * Description of the custom field type. This will be displayed for a user on the create custom field page.
     */
    @Required
    private I18nProperty description;

    @Required
    private IssueFieldType type;

    public IssueFieldType getType()
    {
        return type;
    }

    public I18nProperty getDescription()
    {
        return description;
    }

    public ConnectFieldModuleBean()
    {
        this.type = IssueFieldType.TEXT;
        this.description = I18nProperty.empty();
    }

    public ConnectFieldModuleBean(IssueFieldModuleBeanBuilder builder)
    {
        super(builder);
    }

    public static IssueFieldModuleBeanBuilder newBuilder()
    {
        return new IssueFieldModuleBeanBuilder();
    }
}
