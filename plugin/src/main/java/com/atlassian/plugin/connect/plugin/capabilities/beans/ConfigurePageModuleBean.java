package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.ConfigurePageModuleBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.InvalidAddonConfigurationException;

import static com.atlassian.plugin.connect.plugin.capabilities.util.ModuleKeyGenerator.nameToKey;
import static com.google.common.base.Objects.equal;

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
    public static final String DEFAULT_MODULE_KEY = "config-page";
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

    @Override
    public String getKey()
    {
        String key = super.getKey();
        // TODO: bit dodgy to call nameToKey here but not sure how else to check the key is not being defaulted.
        // We could set the key when setAsDefault on the builder
        // TODO: make sure covered by unit tests
        if (isDefault && key != null && !equal(key, nameToKey(getName().getValue())) && !equal(key, DEFAULT_MODULE_KEY))
        {
            throw new InvalidAddonConfigurationException("Must not specify a key name for default configuration module");
        }
        return isDefault ? DEFAULT_MODULE_KEY : key;
    }

    public static ConfigurePageModuleBeanBuilder newConfigurePageBean()
    {
        return new ConfigurePageModuleBeanBuilder();
    }

    public static ConfigurePageModuleBeanBuilder newConfigurePageBean(ConfigurePageModuleBean defaultBean)
    {
        return new ConfigurePageModuleBeanBuilder(defaultBean);
    }

}
