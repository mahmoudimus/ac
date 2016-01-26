package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.plugin.connect.modules.beans.builder.CustomFieldTypeModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;

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
    /**
     * Description of the custom field type. This will be displayed for a user on the create custom field page.
     */
    @Required
    private I18nProperty description;

    @Required
    private CustomFieldBaseTypeConfiguration baseTypeConfiguration;

    public CustomFieldBaseTypeConfiguration getBaseTypeConfiguration()
    {
        return baseTypeConfiguration;
    }

    public void setBaseTypeConfiguration(CustomFieldBaseTypeConfiguration baseTypeConfiguration)
    {
        this.baseTypeConfiguration = baseTypeConfiguration;
    }

    public I18nProperty getDescription()
    {
        return description;
    }

    public void setDescription(final I18nProperty description)
    {
        this.description = description;
    }

    public CustomFieldTypeModuleBean()
    {
        this.baseTypeConfiguration = new CustomFieldBaseTypeConfiguration();
        this.description = I18nProperty.empty();
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
