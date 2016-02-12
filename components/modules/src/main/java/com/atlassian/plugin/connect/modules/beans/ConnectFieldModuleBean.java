package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.plugin.connect.modules.beans.builder.IssueFieldModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;

/**
 * Connect field module allows the add-on to add a new custom field, which is locked and tied to your plugin.
 * Locked meaning that it cannot be removed by the user, and there is only 1 field instance possible.
 * The field will not appear on screens, you need to add it manually using the screens REST API.
 * When your plugin is disabled/uninstalled the field will disappear from the screens.
 *
 *
 * #### Example
 * @schemaTitle Issue Field
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
