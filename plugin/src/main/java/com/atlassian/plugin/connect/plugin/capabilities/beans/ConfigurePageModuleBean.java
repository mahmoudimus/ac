package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.json.schema.annotation.CommonSchemaAttributes;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.ConfigurePageModuleBeanBuilder;
import com.google.common.base.Objects;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * A configure page module is a page module used to configure the addon itself.
 * Other than that it is the same as other pages.
 *
 * If more than one configure page is specified, then exactly one of them must have the [isDefault] field set.
 * A 'configure' button will link to this page from the manage plugins administration console.
 *
 * If there is only one configure page specified then it will automatically be used regardless of the isDefault field.
 *
 * If no configure page modules are specified then no configure button will be included on the install page.
 */
public class ConfigurePageModuleBean extends ConnectPageModuleBean
{
    /**
     * If there is more than one configure page in an addon then one and only one must be marked as "isDefault" : true
     */
    @CommonSchemaAttributes(defaultValue = "false")
    private Boolean isDefault;

    public ConfigurePageModuleBean()
    {
        init();
    }

    public ConfigurePageModuleBean(ConfigurePageModuleBeanBuilder builder)
    {
        super(builder);

        init();
    }

    private void init()
    {
        if (this.isDefault == null)
        {
            this.isDefault = false;
        }
    }

    public Boolean isDefault()
    {
        return isDefault == null ? false : isDefault;
    }

    @Override
    public boolean equals(Object otherObj)
    {
        if (otherObj == this)
        {
            return true;
        }

        if (!(otherObj instanceof ConfigurePageModuleBean && super.equals(otherObj)))
        {
            return false;
        }

        ConfigurePageModuleBean other = (ConfigurePageModuleBean) otherObj;

        return new EqualsBuilder()
                .append(isDefault(), other.isDefault())
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(73, 57)
                .append(isDefault())
                .build();
    }

    public static ConfigurePageModuleBeanBuilder newConfigurePageBean()
    {
        return new ConfigurePageModuleBeanBuilder();
    }

    public static ConfigurePageModuleBeanBuilder newConfigurePageBean(ConfigurePageModuleBean defaultBean)
    {
        return new ConfigurePageModuleBeanBuilder(defaultBean);
    }

    @Override
    public String toString()
    {
        Objects.ToStringHelper toStringHelper = Objects.toStringHelper(this);
        appendToStringFields(toStringHelper);
        toStringHelper.add("isDefault", isDefault());
        return toStringHelper.toString();
    }


}
