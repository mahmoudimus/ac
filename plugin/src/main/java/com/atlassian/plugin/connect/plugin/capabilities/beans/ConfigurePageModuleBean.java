package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.ConfigurePageModuleBeanBuilder;

/**
 * A configure page module is a page module used to configure the addon itself.
 * Other than that it is the same as other pages.
 *
 * If more than one configure page is specified then exactly one of them must have the [default] field set.
 * This page will be the page shown when the "configure" button is pressed in the add on install page in UPM.
 *
 * If there is only one configure page specified then it will automatically be used regardless of the default field.
 *
 * If no configure page modules are specified then no configure button will be included on the install page.
 */
public class ConfigurePageModuleBean extends ConnectPageModuleBean
{
    private Boolean isDefault; // TODO: ask JD if I can use lil boolean

    public ConfigurePageModuleBean(ConfigurePageModuleBeanBuilder builder)
    {
        super(builder);

        if (this.isDefault == null)
        {
            this.isDefault = false;
        }
    }

    public Boolean isDefault()
    {
        return isDefault;
    }

    public static ConfigurePageModuleBeanBuilder newPageBean()
    {
        return new ConfigurePageModuleBeanBuilder();
    }

    public static ConfigurePageModuleBeanBuilder newPageBean(ConfigurePageModuleBean defaultBean)
    {
        return new ConfigurePageModuleBeanBuilder(defaultBean);
    }

}
