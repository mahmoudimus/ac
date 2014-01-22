package com.atlassian.plugin.connect.plugin.module.websection;

import com.atlassian.confluence.plugin.descriptor.web.descriptors.ConfluenceWebSectionModuleDescriptor;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessor;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.plugin.web.descriptors.WebSectionModuleDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

@ConfluenceComponent
public class ConfluenceWebSectionModuleDescriptorFactory implements ProductSpecificWebSectionModuleDescriptorFactory
{
    @Autowired
    public ConfluenceWebSectionModuleDescriptorFactory()
    {
    }

    @Override
    public WebSectionModuleDescriptor createWebSectionModuleDescriptor()
    {
        return new ConfluenceWebSectionModuleDescriptor();
    }
}
