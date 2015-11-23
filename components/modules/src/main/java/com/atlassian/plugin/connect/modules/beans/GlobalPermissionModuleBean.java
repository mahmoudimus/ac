package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.CommonSchemaAttributes;
import com.atlassian.json.schema.annotation.Required;
import com.atlassian.plugin.connect.modules.beans.builder.GlobalPermissionModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;

/**
 * Create the global permission.
 *
 * Global permissions are useful if you need to manage permissions for operations performed on global objects
 * like users or global settings.
 *
 * User's permissions can be checked using <a href="https://docs.atlassian.com/jira/REST/latest/#api/2/-getPermissions">my permission resource</a>.
 * Global permissions are not supported in conditions yet.
 *
 * #### Example
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
