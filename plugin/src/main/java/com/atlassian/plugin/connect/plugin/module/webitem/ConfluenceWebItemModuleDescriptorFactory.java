package com.atlassian.plugin.connect.plugin.module.webitem;

import com.atlassian.confluence.plugin.descriptor.web.descriptors.ConfluenceWebItemModuleDescriptor;
import com.atlassian.confluence.plugin.descriptor.web.model.ConfluenceWebLink;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.plugin.module.context.ContextMapURLSerializer;
import com.atlassian.plugin.web.WebFragmentHelper;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Creates ConfluenceWebItemModuleDescriptor with link pointing to remote plugin.
 */
public class ConfluenceWebItemModuleDescriptorFactory implements WebItemModuleDescriptorFactory
{
    private final UrlVariableSubstitutor urlVariableSubstitutor;
    private final ContextMapURLSerializer contextMapURLSerializer;
    private final WebFragmentHelper webFragmentHelper;

    public ConfluenceWebItemModuleDescriptorFactory(
            UrlVariableSubstitutor urlVariableSubstitutor,
            ContextMapURLSerializer contextMapURLSerializer,
            WebFragmentHelper webFragmentHelper)
    {
        this.webFragmentHelper = checkNotNull(webFragmentHelper);
        this.contextMapURLSerializer = checkNotNull(contextMapURLSerializer);
        this.urlVariableSubstitutor = checkNotNull(urlVariableSubstitutor);
    }

    @Override
    public WebItemModuleDescriptor createWebItemModuleDescriptor(String url, String moduleKey, boolean absolute)
    {
        return new RemoteConfluenceWebItemModuleDescriptor(urlVariableSubstitutor, contextMapURLSerializer, contextMapURLSerializer, webFragmentHelper, url, moduleKey, absolute);
    }

    private static final class RemoteConfluenceWebItemModuleDescriptor extends ConfluenceWebItemModuleDescriptor
    {
        private final UrlVariableSubstitutor urlVariableSubstitutor;
        private final ContextMapURLSerializer urlParametersSerializer;
        private final ContextMapURLSerializer contextMapURLSerializer;
        private final WebFragmentHelper webFragmentHelper;
        private final String url;
        private final String moduleKey;
        private final boolean absolute;

        private RemoteConfluenceWebItemModuleDescriptor(
                UrlVariableSubstitutor urlVariableSubstitutor,
                ContextMapURLSerializer urlParametersSerializer,
                ContextMapURLSerializer contextMapURLSerializer,
                WebFragmentHelper webFragmentHelper,
                String url,
                String moduleKey,
                boolean absolute)
        {
            this.urlVariableSubstitutor = urlVariableSubstitutor;
            this.urlParametersSerializer = urlParametersSerializer;
            this.contextMapURLSerializer = contextMapURLSerializer;
            this.webFragmentHelper = webFragmentHelper;
            this.url = url;
            this.moduleKey = moduleKey;
            this.absolute = absolute;
        }

        @Override
        public ConfluenceWebLink getLink()
        {
            return new ConfluenceWebLink(new RemoteWebLink(this, webFragmentHelper, urlVariableSubstitutor, contextMapURLSerializer, url, moduleKey, absolute));
        }
    }
}
