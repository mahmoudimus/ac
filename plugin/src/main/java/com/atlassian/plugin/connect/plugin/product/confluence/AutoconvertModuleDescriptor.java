package com.atlassian.plugin.connect.plugin.product.confluence;

import com.atlassian.plugin.connect.modules.beans.nested.AutoconvertBean;
import com.atlassian.plugin.connect.modules.beans.nested.MatcherBean;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;

/**
 * A module descriptor to hold the autoconvert declaration.
 */
public class AutoconvertModuleDescriptor extends AbstractModuleDescriptor<AutoconvertBean>
{
    private final String macroName;
    private final AutoconvertBean autoconvertBean;
    private final MatcherBean matcherBean;

    public AutoconvertModuleDescriptor(ModuleFactory moduleFactory, String macroName, AutoconvertBean autoconvertBean, MatcherBean matcherBean)
    {
        super(moduleFactory);
        this.macroName = macroName;
        this.autoconvertBean = autoconvertBean;
        this.matcherBean = matcherBean;
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

    public MatcherBean getMatcherBean() {
        return matcherBean;
    }
}
