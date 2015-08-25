package com.atlassian.plugin.connect.util.fixture.descriptor;

import com.atlassian.plugin.connect.api.module.webitem.WebItemModuleDescriptorData;
import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.spi.module.webitem.ProductSpecificWebItemModuleDescriptorFactory;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.DefaultWebItemModuleDescriptor;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;

/**
 * @since 1.0
 */
public class WebItemModuleDescriptorFactoryForTests implements ProductSpecificWebItemModuleDescriptorFactory
{
    private WebInterfaceManager webInterfaceManager;

    public WebItemModuleDescriptorFactoryForTests(WebInterfaceManager webInterfaceManager)
    {
        this.webInterfaceManager = webInterfaceManager;
    }

    @Override
    public WebItemModuleDescriptor createWebItemModuleDescriptor(final WebItemModuleDescriptorData webItemModuleDescriptorData)
    {
        return new DefaultWebItemModuleDescriptor(webInterfaceManager);
    }
}
