package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.Required;

/**
 *
 * @exampleJson {@link com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#DASHBOARD_ITEM_EXAMPLE}
 * @schemaTitle Custom Field Type
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
}
