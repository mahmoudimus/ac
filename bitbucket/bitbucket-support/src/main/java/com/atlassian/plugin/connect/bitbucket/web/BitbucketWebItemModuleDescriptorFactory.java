package com.atlassian.plugin.connect.bitbucket.web;

import com.atlassian.plugin.connect.api.web.PluggableParametersExtractor;
import com.atlassian.plugin.connect.api.web.RemoteWebLink;
import com.atlassian.plugin.connect.api.web.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.api.web.context.ModuleContextFilter;
import com.atlassian.plugin.connect.api.web.iframe.IFrameUriBuilderFactory;
import com.atlassian.plugin.connect.modules.beans.AddonUrlContext;
import com.atlassian.plugin.connect.spi.web.item.ProductSpecificWebItemModuleDescriptorFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.BitbucketComponent;
import com.atlassian.plugin.web.WebFragmentHelper;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.DefaultWebItemModuleDescriptor;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.plugin.web.model.WebLink;
import org.springframework.beans.factory.annotation.Autowired;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Creates BitbuckethWebItemModuleDescriptor with link pointing to remote plugin.
 */
@BitbucketComponent
public class BitbucketWebItemModuleDescriptorFactory implements ProductSpecificWebItemModuleDescriptorFactory
{
    private final IFrameUriBuilderFactory iFrameUriBuilderFactory;
    private final ModuleContextFilter moduleContextFilter;
    private final UrlVariableSubstitutor urlVariableSubstitutor;
    private final WebFragmentHelper webFragmentHelper;
    private final PluggableParametersExtractor webFragmentModuleContextExtractor;
    private final WebInterfaceManager webInterfaceManager;

    @Autowired
    public BitbucketWebItemModuleDescriptorFactory(
            WebFragmentHelper webFragmentHelper,
            WebInterfaceManager webInterfaceManager,
            IFrameUriBuilderFactory iFrameUriBuilderFactory,
            PluggableParametersExtractor webFragmentModuleContextExtractor,
            ModuleContextFilter moduleContextFilter,
            UrlVariableSubstitutor urlVariableSubstitutor)
    {
        this.urlVariableSubstitutor = urlVariableSubstitutor;
        this.webFragmentModuleContextExtractor = checkNotNull(webFragmentModuleContextExtractor);
        this.moduleContextFilter = checkNotNull(moduleContextFilter);
        this.iFrameUriBuilderFactory = checkNotNull(iFrameUriBuilderFactory);
        this.webInterfaceManager = checkNotNull(webInterfaceManager);
        this.webFragmentHelper = checkNotNull(webFragmentHelper);
    }

    @Override
    public WebItemModuleDescriptor createWebItemModuleDescriptor(String url, String pluginKey, String moduleKey, boolean absolute, final AddonUrlContext addOnUrlContext, boolean isDialog, String section) {
        return new RemoteBitbucketWebItemModuleDescriptor(webInterfaceManager, webFragmentHelper,
                iFrameUriBuilderFactory, urlVariableSubstitutor, webFragmentModuleContextExtractor, moduleContextFilter,
                url, pluginKey, moduleKey, absolute, addOnUrlContext, isDialog, section);
    }

    private static class RemoteBitbucketWebItemModuleDescriptor extends DefaultWebItemModuleDescriptor
    {
        private final AddonUrlContext addOnUrlContext;
        private final boolean dialog;
        private final IFrameUriBuilderFactory iFrameUriBuilderFactory;
        private final ModuleContextFilter moduleContextFilter;
        private final String moduleKey;
        private final String pluginKey;
        private final String section;
        private final String url;
        private final UrlVariableSubstitutor urlVariableSubstitutor;
        private final WebFragmentHelper webFragmentHelper;
        private final PluggableParametersExtractor webFragmentModuleContextExtractor;

        private boolean absolute;

        public RemoteBitbucketWebItemModuleDescriptor(
                WebInterfaceManager webInterfaceManager,
                WebFragmentHelper webFragmentHelper,
                IFrameUriBuilderFactory iFrameUriBuilderFactory,
                UrlVariableSubstitutor urlVariableSubstitutor,
                PluggableParametersExtractor webFragmentModuleContextExtractor,
                ModuleContextFilter moduleContextFilter,
                String url,
                String pluginKey,
                String moduleKey,
                boolean absolute, AddonUrlContext addOnUrlContext, boolean dialog, String section)
        {
            super(webInterfaceManager);

            this.absolute = absolute;
            this.addOnUrlContext = addOnUrlContext;
            this.dialog = dialog;
            this.iFrameUriBuilderFactory = iFrameUriBuilderFactory;
            this.moduleKey = moduleKey;
            this.moduleContextFilter = moduleContextFilter;
            this.pluginKey = pluginKey;
            this.section = section;
            this.url = url;
            this.urlVariableSubstitutor = urlVariableSubstitutor;
            this.webFragmentHelper = webFragmentHelper;
            this.webFragmentModuleContextExtractor = webFragmentModuleContextExtractor;
        }

        @Override
        public WebLink getLink()
        {
            return new RemoteWebLink(this, webFragmentHelper, iFrameUriBuilderFactory,
                    urlVariableSubstitutor, webFragmentModuleContextExtractor, moduleContextFilter, url, pluginKey,
                    moduleKey, absolute, addOnUrlContext, dialog);
        }
    }

}
