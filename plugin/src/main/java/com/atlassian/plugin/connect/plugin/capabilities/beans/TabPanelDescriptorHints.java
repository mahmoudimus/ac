package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.ModuleDescriptor;

public class TabPanelDescriptorHints
{
    private final String modulePrefix;
    private final String domElementName;
    private final Class<? extends ModuleDescriptor> descriptorClass;
    private final Class<?> moduleClass;

    public TabPanelDescriptorHints()
    {
        this("", "", ModuleDescriptor.class, null);
    }

    public TabPanelDescriptorHints(String modulePrefix, String domElementName, Class<? extends ModuleDescriptor> descriptorClass, Class<?> moduleClass)
    {
        this.modulePrefix = modulePrefix;
        this.domElementName = domElementName;
        this.descriptorClass = descriptorClass;
        this.moduleClass = moduleClass;
    }

    public String getModulePrefix()
    {
        return modulePrefix;
    }

    public Class<? extends ModuleDescriptor> getDescriptorClass()
    {
        return descriptorClass;
    }

    public Class<?> getModuleClass()
    {
        return moduleClass;
    }

    public String getDomElementName()
    {
        return domElementName;
    }
}
