package com.atlassian.plugin.connect.jira.iframe.tabpanel;

import com.atlassian.plugin.ModuleDescriptor;

public class TabPanelDescriptorHints
{
    private final String domElementName;
    private final Class<? extends ModuleDescriptor> descriptorClass;
    private final Class<?> moduleClass;

    public TabPanelDescriptorHints(String domElementName, Class<? extends ModuleDescriptor> descriptorClass, Class<?> moduleClass)
    {
        this.domElementName = domElementName;
        this.descriptorClass = descriptorClass;
        this.moduleClass = moduleClass;
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
