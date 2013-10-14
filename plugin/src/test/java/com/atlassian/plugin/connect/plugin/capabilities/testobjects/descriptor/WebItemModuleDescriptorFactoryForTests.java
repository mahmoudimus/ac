package com.atlassian.plugin.connect.plugin.capabilities.testobjects.descriptor;

import com.atlassian.plugin.connect.plugin.module.webitem.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.DefaultWebItemModuleDescriptor;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;

import static org.mockito.Mockito.mock;

/**
 * @since version
 */
public class WebItemModuleDescriptorFactoryForTests implements WebItemModuleDescriptorFactory
{
    private WebInterfaceManager webInterfaceManager;

    public WebItemModuleDescriptorFactoryForTests(WebInterfaceManager webInterfaceManager)
    {
        this.webInterfaceManager = webInterfaceManager;
    }

    @Override
    public WebItemModuleDescriptor createWebItemModuleDescriptor(String url, String linkId, boolean absolute)
    {
        return new DefaultWebItemModuleDescriptor(webInterfaceManager);
    }
}
