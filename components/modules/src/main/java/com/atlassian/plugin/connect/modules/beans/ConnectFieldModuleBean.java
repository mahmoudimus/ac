package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.plugin.connect.modules.beans.builder.IssueFieldModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;

/**
 * This module allows Connect add-ons to add their own fields to JIRA issues.
 * Such fields are treated as locked custom fields, meaning that they cannot be removed
 * by JIRA admins. There is always one and only one instance of the field created and
 * managed by JIRA.
 *
 * <p>
 *     The field is automatically removed when your add-on is uninstalled and then
 *     restored (together with all previously set values) if installed again.
 * </p>
 *
 * <p>
 *     The field will not appear on screens on its own.
 *     It can be added by your add-on via the screens REST API
 *     or manually by JIRA admins.
 * </p>
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
