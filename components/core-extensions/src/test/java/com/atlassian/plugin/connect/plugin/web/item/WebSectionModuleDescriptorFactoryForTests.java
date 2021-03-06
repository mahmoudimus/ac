package com.atlassian.plugin.connect.plugin.web.item;

import com.atlassian.plugin.connect.spi.web.item.ProductSpecificWebSectionModuleDescriptorFactory;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.DefaultWebSectionModuleDescriptor;
import com.atlassian.plugin.web.descriptors.WebSectionModuleDescriptor;

/**
 * @since 1.0
 */
public class WebSectionModuleDescriptorFactoryForTests implements ProductSpecificWebSectionModuleDescriptorFactory {
    private WebInterfaceManager webInterfaceManager;

    public WebSectionModuleDescriptorFactoryForTests(WebInterfaceManager webInterfaceManager) {
        this.webInterfaceManager = webInterfaceManager;
    }

    @Override
    public WebSectionModuleDescriptor createWebSectionModuleDescriptor() {
        return new DefaultWebSectionModuleDescriptor(webInterfaceManager);
    }
}
