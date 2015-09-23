package com.atlassian.plugin.connect.bitbucket.web;

import com.atlassian.plugin.connect.spi.web.item.ProductSpecificWebSectionModuleDescriptorFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.BitbucketComponent;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.DefaultWebSectionModuleDescriptor;
import com.atlassian.plugin.web.descriptors.WebSectionModuleDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

@BitbucketComponent
public class BitbucketWebSectionModuleDescriptorFactory implements ProductSpecificWebSectionModuleDescriptorFactory
{
    private final WebInterfaceManager webInterfaceManager;

    @Autowired
    public BitbucketWebSectionModuleDescriptorFactory(WebInterfaceManager webInterfaceManager) {
        this.webInterfaceManager = webInterfaceManager;
    }

    @Override
    public WebSectionModuleDescriptor createWebSectionModuleDescriptor()
    {
        return new DefaultWebSectionModuleDescriptor(webInterfaceManager);
    }

}
