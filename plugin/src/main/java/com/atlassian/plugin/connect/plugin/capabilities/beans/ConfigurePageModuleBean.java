package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.ConfigurePageModuleBeanBuilder;
import com.google.common.base.Objects;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * A configure page module is a page module used to configure the addon itself.
 * Other than that it is the same as other pages.
 * <p/>
 * If more than one configure page is specified, then exactly one of them must have the [default] field set.
 * A 'configure' button will link to this page from the manage plugins administration console.
 * <p/>
 * If there is only one configure page specified then it will automatically be used regardless of the default field.
 * <p/>
 * If no configure page modules are specified then no configure button will be included on the install page.
 */
public class ConfigurePageModuleBean extends ConnectPageModuleBean
{
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

    /**
     * If there is more than one configure page in an addon then one and only one must be marked as default=true
     */
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
