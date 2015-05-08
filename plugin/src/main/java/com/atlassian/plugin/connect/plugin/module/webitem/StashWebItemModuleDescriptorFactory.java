package com.atlassian.plugin.connect.plugin.module.webitem;

import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextFilter;
import com.atlassian.plugin.connect.plugin.iframe.render.uri.IFrameUriBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.webpanel.PluggableParametersExtractor;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.spring.scanner.annotation.component.StashComponent;
import com.atlassian.plugin.web.WebFragmentHelper;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.DefaultWebItemModuleDescriptor;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.plugin.web.model.WebLink;
import org.springframework.beans.factory.annotation.Autowired;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Creates StashWebItemModuleDescriptor with link pointing to remote plugin.
 */
@StashComponent
public class StashWebItemModuleDescriptorFactory implements ProductSpecificWebItemModuleDescriptorFactory
{
    private final IFrameUriBuilderFactory iFrameUriBuilderFactory;
    private final ModuleContextFilter moduleContextFilter;
    private final UrlVariableSubstitutor urlVariableSubstitutor;
    private final WebFragmentHelper webFragmentHelper;
    private final PluggableParametersExtractor webFragmentModuleContextExtractor;
    private final WebInterfaceManager webInterfaceManager;

    @Autowired
    public StashWebItemModuleDescriptorFactory(
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
    public WebItemModuleDescriptor createWebItemModuleDescriptor(String url, String pluginKey, String moduleKey, boolean absolute, AddOnUrlContext addOnUrlContext, boolean isDialog)
    {
        return new RemoteStashWebItemModuleDescriptor(webInterfaceManager, webFragmentHelper,
                iFrameUriBuilderFactory, urlVariableSubstitutor, webFragmentModuleContextExtractor, moduleContextFilter,
                url, pluginKey, moduleKey, absolute, addOnUrlContext, isDialog);
    }

    private static final class RemoteStashWebItemModuleDescriptor extends DefaultWebItemModuleDescriptor
    {
        private final WebFragmentHelper webFragmentHelper;
        private final IFrameUriBuilderFactory iFrameUriBuilderFactory;
        private final UrlVariableSubstitutor urlVariableSubstitutor;
        private final PluggableParametersExtractor webFragmentModuleContextExtractor;
        private final ModuleContextFilter moduleContextFilter;
        private final String url;
        private final String pluginKey;
        private final String moduleKey;
        private boolean absolute;
        private final AddOnUrlContext addOnUrlContext;
        private final boolean isDialog;

        public RemoteStashWebItemModuleDescriptor(
                WebInterfaceManager webInterfaceManager,
                WebFragmentHelper webFragmentHelper,
                IFrameUriBuilderFactory iFrameUriBuilderFactory,
                UrlVariableSubstitutor urlVariableSubstitutor,
                PluggableParametersExtractor webFragmentModuleContextExtractor,
                ModuleContextFilter moduleContextFilter,
                String url,
                String pluginKey,
                String moduleKey,
                boolean absolute, AddOnUrlContext addOnUrlContext, boolean isDialog)
        {
            super(webInterfaceManager);
            this.webFragmentHelper = webFragmentHelper;
            this.iFrameUriBuilderFactory = iFrameUriBuilderFactory;
            this.urlVariableSubstitutor = urlVariableSubstitutor;
            this.webFragmentModuleContextExtractor = webFragmentModuleContextExtractor;
            this.moduleContextFilter = moduleContextFilter;
            this.url = url;
            this.pluginKey = pluginKey;
            this.moduleKey = moduleKey;
            this.absolute = absolute;
            this.addOnUrlContext = addOnUrlContext;
            this.isDialog = isDialog;
        }

        @Override
        public WebLink getLink()
        {
            return new RemoteWebLink(this, webFragmentHelper, iFrameUriBuilderFactory,
                    urlVariableSubstitutor, webFragmentModuleContextExtractor, moduleContextFilter, url, pluginKey,
                    moduleKey, absolute, addOnUrlContext, isDialog);
        }

        @Override
        public void destroy()
        {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }

}
