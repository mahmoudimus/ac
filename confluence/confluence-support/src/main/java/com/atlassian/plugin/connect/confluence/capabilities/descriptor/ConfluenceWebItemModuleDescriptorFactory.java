package com.atlassian.plugin.connect.confluence.capabilities.descriptor;

import com.atlassian.confluence.plugin.descriptor.web.descriptors.ConfluenceWebItemModuleDescriptor;
import com.atlassian.confluence.plugin.descriptor.web.model.ConfluenceWebLink;
import com.atlassian.plugin.connect.api.module.webitem.WebItemModuleDescriptorData;
import com.atlassian.plugin.connect.api.module.webitem.WebLinkFactory;
import com.atlassian.plugin.connect.spi.module.webitem.ProductSpecificWebItemModuleDescriptorFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.plugin.web.WebFragmentHelper;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.plugin.web.model.WebLink;
import com.atlassian.sal.api.component.ComponentLocator;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Creates ConfluenceWebItemModuleDescriptor with link pointing to remote plugin.
 */
@ConfluenceComponent
public class ConfluenceWebItemModuleDescriptorFactory implements ProductSpecificWebItemModuleDescriptorFactory
{
    private final WebLinkFactory webLinkFactory;

    @Autowired
    public ConfluenceWebItemModuleDescriptorFactory(WebLinkFactory webLinkFactory)
    {
        this.webLinkFactory = webLinkFactory;
    }

    @Override
    public WebItemModuleDescriptor createWebItemModuleDescriptor(WebItemModuleDescriptorData webItemModuleDescriptorData)
    {
        WebFragmentHelper webFragmentHelper = ComponentLocator.getComponent(WebFragmentHelper.class);
        return new RemoteConfluenceWebItemModuleDescriptor(webLinkFactory, webItemModuleDescriptorData);
    }

    private static final class RemoteConfluenceWebItemModuleDescriptor extends ConfluenceWebItemModuleDescriptor
    {
        private final WebLinkFactory webLinkFactory;
        private final WebItemModuleDescriptorData webItemModuleDescriptorData;

        private RemoteConfluenceWebItemModuleDescriptor(WebLinkFactory webLinkFactory, WebItemModuleDescriptorData webItemModuleDescriptorData)
        {
            this.webLinkFactory = webLinkFactory;
            this.webItemModuleDescriptorData = webItemModuleDescriptorData;
        }

        @Override
        public ConfluenceWebLink getLink()
        {
            WebLink remoteWebLink = webLinkFactory.createRemoteWebLink(this, webItemModuleDescriptorData);
            return new ConfluenceWebLink(remoteWebLink);
        }

        @Override
        public void destroy()
        {
        }
    }
}
