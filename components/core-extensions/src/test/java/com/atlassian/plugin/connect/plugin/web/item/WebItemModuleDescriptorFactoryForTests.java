package com.atlassian.plugin.connect.plugin.web.item;

import com.atlassian.plugin.connect.modules.beans.AddonUrlContext;
import com.atlassian.plugin.connect.spi.web.item.ProductSpecificWebItemModuleDescriptorFactory;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.DefaultWebItemModuleDescriptor;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;

/**
 * @since 1.0
 */
public class WebItemModuleDescriptorFactoryForTests implements ProductSpecificWebItemModuleDescriptorFactory {
    private WebInterfaceManager webInterfaceManager;

    public WebItemModuleDescriptorFactoryForTests(WebInterfaceManager webInterfaceManager) {
        this.webInterfaceManager = webInterfaceManager;
    }

    @Override
    public WebItemModuleDescriptor createWebItemModuleDescriptor(final String url, final String pluginKey, final String moduleKey, final boolean absolute, final AddonUrlContext addonUrlContext, final boolean isDialog, final String section) {
        return new DefaultWebItemModuleDescriptor(webInterfaceManager);
    }
}
