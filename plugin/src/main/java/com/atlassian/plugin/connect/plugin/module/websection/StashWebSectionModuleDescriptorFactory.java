package com.atlassian.plugin.connect.plugin.module.websection;

import com.atlassian.plugin.spring.scanner.annotation.component.StashComponent;
import com.atlassian.plugin.web.descriptors.WebSectionModuleDescriptor;

@StashComponent
public class StashWebSectionModuleDescriptorFactory implements ProductSpecificWebSectionModuleDescriptorFactory
{
    @Override
    public WebSectionModuleDescriptor createWebSectionModuleDescriptor()
    {
        return null;
    }
}
