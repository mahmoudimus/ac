package com.atlassian.plugin.connect.confluence.capabilities.descriptor;

import com.atlassian.confluence.plugin.descriptor.web.descriptors.ConfluenceWebItemModuleDescriptor;
import com.atlassian.confluence.plugin.descriptor.web.model.ConfluenceWebLink;
import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextFilter;
import com.atlassian.plugin.connect.plugin.iframe.render.uri.IFrameUriBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.webpanel.PluggableParametersExtractor;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.plugin.module.webitem.ProductSpecificWebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.module.webitem.RemoteWebLink;
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
    private final PluggableParametersExtractor webFragmentModuleContextExtractor;
    private final ModuleContextFilter moduleContextFilter;
    private final UrlVariableSubstitutor urlVariableSubstitutor;

    @Autowired
    public ConfluenceWebItemModuleDescriptorFactory(
            WebFragmentHelper webFragmentHelper,
            IFrameUriBuilderFactory iFrameUriBuilderFactory,
            PluggableParametersExtractor webFragmentModuleContextExtractor,
            ModuleContextFilter moduleContextFilter,
            UrlVariableSubstitutor urlVariableSubstitutor)
    {
        this.urlVariableSubstitutor = checkNotNull(urlVariableSubstitutor);
        this.iFrameUriBuilderFactory = checkNotNull(iFrameUriBuilderFactory);
        this.webFragmentModuleContextExtractor = checkNotNull(webFragmentModuleContextExtractor);
        this.moduleContextFilter = checkNotNull(moduleContextFilter);
        this.webFragmentHelper = checkNotNull(webFragmentHelper);
    }

    @Override
    public WebItemModuleDescriptor createWebItemModuleDescriptor(String url, String pluginKey, String moduleKey, boolean absolute, AddOnUrlContext addOnUrlContext, boolean isDialog)
    {
        return new RemoteConfluenceWebItemModuleDescriptor(
                webFragmentHelper
                , iFrameUriBuilderFactory
                , webFragmentModuleContextExtractor
                , moduleContextFilter
                , urlVariableSubstitutor
                , url
                , pluginKey
                , moduleKey
                , absolute
                , addOnUrlContext
                , isDialog);
    }

    private static final class RemoteConfluenceWebItemModuleDescriptor extends ConfluenceWebItemModuleDescriptor
    {
        private final WebFragmentHelper webFragmentHelper;
        private final IFrameUriBuilderFactory iFrameUriBuilderFactory;
        private final PluggableParametersExtractor webFragmentModuleContextExtractor;
        private final ModuleContextFilter moduleContextFilter;
        private final String url;
        private final UrlVariableSubstitutor urlVariableSubstitutor;
        private final String pluginKey;
        private final String moduleKey;
        private final boolean absolute;
        private final AddOnUrlContext addOnUrlContext;
        private final boolean isDialog;

        private RemoteConfluenceWebItemModuleDescriptor(
                WebFragmentHelper webFragmentHelper,
                IFrameUriBuilderFactory iFrameUriBuilderFactory,
                PluggableParametersExtractor webFragmentModuleContextExtractor, ModuleContextFilter moduleContextFilter,
                UrlVariableSubstitutor urlVariableSubstitutor, String url, String pluginKey, String moduleKey, boolean absolute,
                AddOnUrlContext addOnUrlContext, boolean isDialog)
        {
            this.iFrameUriBuilderFactory = iFrameUriBuilderFactory;
            this.webFragmentModuleContextExtractor = webFragmentModuleContextExtractor;
            this.moduleContextFilter = moduleContextFilter;
            this.urlVariableSubstitutor = urlVariableSubstitutor;
            this.pluginKey = pluginKey;
            this.moduleKey = moduleKey;
            this.webFragmentHelper = webFragmentHelper;
            this.url = url;
            this.absolute = absolute;
            this.addOnUrlContext = addOnUrlContext;
            this.isDialog = isDialog;
        }

        @Override
        public ConfluenceWebLink getLink()
        {
            return new ConfluenceWebLink(new RemoteWebLink(this, webFragmentHelper, iFrameUriBuilderFactory, urlVariableSubstitutor,
                    webFragmentModuleContextExtractor, moduleContextFilter, url, pluginKey, moduleKey, absolute, addOnUrlContext, isDialog));
        }

        @Override
        public void destroy()
        {

        }
    }
}
