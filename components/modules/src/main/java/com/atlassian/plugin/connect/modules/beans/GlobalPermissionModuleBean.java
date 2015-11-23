package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.CommonSchemaAttributes;
import com.atlassian.json.schema.annotation.Required;
import com.atlassian.plugin.connect.modules.beans.builder.GlobalPermissionModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Create the global permission.
 *
 * Global permissions are useful if you need to manage permissions for operations performed on global objects
 * like users or global settings.
 *
 * User's permissions can be checked using [/mypermissions](https://docs.atlassian.com/jira/REST/latest/#api/2/-getPermissions).
 * __Note:__ Global permissions added using this module are not supported for use with the `has_project_permission` condition.
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

    @Override
    public boolean equals(final Object otherObj)
    {
        if (this == otherObj) { return true; }

        if (otherObj == null || getClass() != otherObj.getClass()) { return false; }

        final GlobalPermissionModuleBean that = (GlobalPermissionModuleBean) otherObj;

        return new EqualsBuilder()
                .appendSuper(super.equals(otherObj))
                .append(description, that.description)
                .append(anonymousAllowed, that.anonymousAllowed)
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(description)
                .append(anonymousAllowed)
                .toHashCode();
    }
}
