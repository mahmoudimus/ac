package com.atlassian.plugin.connect.plugin.module.webitem;

import com.atlassian.confluence.plugin.descriptor.web.descriptors.ConfluenceWebItemModuleDescriptor;
import com.atlassian.confluence.plugin.descriptor.web.model.ConfluenceWebLink;
import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextFilter;
import com.atlassian.plugin.connect.plugin.iframe.render.uri.IFrameUriBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.webpanel.WebPanelModuleContextExtractor;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.plugin.web.WebFragmentHelper;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;

import org.springframework.beans.factory.annotation.Autowired;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Creates ConfluenceWebItemModuleDescriptor with link pointing to remote plugin.
 */
@ConfluenceComponent
public class ConfluenceWebItemModuleDescriptorFactory implements ProductSpecificWebItemModuleDescriptorFactory
{
    private final WebFragmentHelper webFragmentHelper;
    private final IFrameUriBuilderFactory iFrameUriBuilderFactory;
    private final WebPanelModuleContextExtractor webPanelModuleContextExtractor;
    private final ModuleContextFilter moduleContextFilter;

    @Autowired
    public ConfluenceWebItemModuleDescriptorFactory(
            WebFragmentHelper webFragmentHelper,
            IFrameUriBuilderFactory iFrameUriBuilderFactory,
            WebPanelModuleContextExtractor webPanelModuleContextExtractor,
            ModuleContextFilter moduleContextFilter)
    {
        this.iFrameUriBuilderFactory = checkNotNull(iFrameUriBuilderFactory);
        this.webPanelModuleContextExtractor = checkNotNull(webPanelModuleContextExtractor);
        this.moduleContextFilter = checkNotNull(moduleContextFilter);
        this.webFragmentHelper = checkNotNull(webFragmentHelper);
    }

    @Override
    public WebItemModuleDescriptor createWebItemModuleDescriptor(String url, String pluginKey, String moduleKey, boolean absolute, AddOnUrlContext addOnUrlContext)
    {
        return new RemoteConfluenceWebItemModuleDescriptor(
                webFragmentHelper
                , iFrameUriBuilderFactory
                , webPanelModuleContextExtractor
                , moduleContextFilter
                , url
                , pluginKey
                , moduleKey
                , absolute
                , addOnUrlContext
        );
    }


    private static final class RemoteConfluenceWebItemModuleDescriptor extends ConfluenceWebItemModuleDescriptor
    {
        private final WebFragmentHelper webFragmentHelper;
        private final IFrameUriBuilderFactory iFrameUriBuilderFactory;
        private final WebPanelModuleContextExtractor webPanelModuleContextExtractor;
        private final ModuleContextFilter moduleContextFilter;
        private final String url;
        private final String pluginKey;
        private final String moduleKey;
        private final boolean absolute;
        private final AddOnUrlContext addOnUrlContext;

        private RemoteConfluenceWebItemModuleDescriptor(
                WebFragmentHelper webFragmentHelper,
                IFrameUriBuilderFactory iFrameUriBuilderFactory, WebPanelModuleContextExtractor webPanelModuleContextExtractor, ModuleContextFilter moduleContextFilter, String url,
                String pluginKey, String moduleKey, boolean absolute,
                AddOnUrlContext addOnUrlContext)
        {
            this.iFrameUriBuilderFactory = iFrameUriBuilderFactory;
            this.webPanelModuleContextExtractor = webPanelModuleContextExtractor;
            this.moduleContextFilter = moduleContextFilter;
            this.pluginKey = pluginKey;
            this.moduleKey = moduleKey;
            this.webFragmentHelper = webFragmentHelper;
            this.url = url;
            this.absolute = absolute;
            this.addOnUrlContext = addOnUrlContext;
        }

        @Override
        public ConfluenceWebLink getLink()
        {
            return new ConfluenceWebLink(new RemoteWebLink(this, webFragmentHelper, iFrameUriBuilderFactory, webPanelModuleContextExtractor, moduleContextFilter, url, pluginKey, moduleKey, absolute, addOnUrlContext));
        }

        @Override
        public void destroy()
        {

        }
    }
}
