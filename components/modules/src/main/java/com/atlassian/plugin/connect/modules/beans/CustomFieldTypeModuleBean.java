package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.plugin.connect.modules.beans.builder.CustomFieldTypeModuleBeanBuilder;

/**
 * Custom field type module allows the add-on to add new custom field types to JIRA.
 *
 * #### Example
 * @schemaTitle Custom Field Type
 * @exampleJson {@link com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#CUSTOM_FIELD_TYPE_EXAMPLE}
 * @since 1.0
 */
public class CustomFieldTypeModuleBean extends RequiredKeyBean
{
    @Required
    private String type;

    public String getType()
    {
        return type;
    }

    public void setType(final String type)
    {
        this.type = type;
    }

    public CustomFieldTypeModuleBean()
    {
        this.type = "";
    }

    public CustomFieldTypeModuleBean(CustomFieldTypeModuleBeanBuilder builder)
    {
        super(builder);
    }

    public static CustomFieldTypeModuleBeanBuilder newBuilder()
    {
        return new CustomFieldTypeModuleBeanBuilder();
    }
}
