package com.atlassian.plugin.connect.plugin.product.confluence;

import com.atlassian.plugin.connect.modules.beans.nested.AutoconvertBean;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;

/**
 * A module descriptor to hold the autoconvert declaration.
 */
public class AutoconvertModuleDescriptor extends AbstractModuleDescriptor<AutoconvertBean>
{
    private final String macroName;
    private final AutoconvertBean autoconvertBean;

    public AutoconvertModuleDescriptor(ModuleFactory moduleFactory, String macroName, AutoconvertBean autoconvertBean)
    {
        super(moduleFactory);
        this.macroName = macroName;
        this.autoconvertBean = autoconvertBean;
    }


    @Override
    public AutoconvertBean getModule()
    {
        return autoconvertBean;
    }

    public String getMacroName()
    {
        return macroName;
    }
}
