package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.CommonSchemaAttributes;
import com.atlassian.json.schema.annotation.Required;
import com.atlassian.plugin.connect.modules.beans.builder.GlobalPermissionModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;

/**
 * Add the permission to the global permission list in JIRA's setting page.
 * Global permissions can not be used in conditions yet.
 *
 * @exampleJson {@link ConnectJsonExamples#GLOBAL_PERMISSION_EXAMPLE}
 * @schemaTitle Global Permission
 * @since 1.1
 */
public class GlobalPermissionModuleBean extends RequiredKeyBean
{
    /**
     * Description of the global permission. It will be displayed under the permission's name.
     */
    @Required
    private I18nProperty description;

    /**
     * Specifies if this permission can be granted to anonymous users.
     */
    @CommonSchemaAttributes (defaultValue = "true")
    private Boolean anonymousAllowed;

    public GlobalPermissionModuleBean()
    {
        super();
        this.description = I18nProperty.empty();
        this.anonymousAllowed = true;
    }

    public GlobalPermissionModuleBean(GlobalPermissionModuleBeanBuilder builder)
    {
        super(builder);
    }

    public static GlobalPermissionModuleBeanBuilder newGlobalPermissionModuleBean()
    {
        return new GlobalPermissionModuleBeanBuilder();
    }

    public I18nProperty getDescription()
    {
        return description;
    }

    public Boolean getAnonymousAllowed()
    {
        return anonymousAllowed;
    }
}
