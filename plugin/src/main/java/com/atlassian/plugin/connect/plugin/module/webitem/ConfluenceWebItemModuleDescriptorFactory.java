package com.atlassian.plugin.connect.plugin.module.webitem;

import com.atlassian.confluence.plugin.descriptor.web.descriptors.ConfluenceWebItemModuleDescriptor;
import com.atlassian.confluence.plugin.descriptor.web.model.ConfluenceWebLink;
import com.atlassian.plugin.connect.plugin.capabilities.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.plugin.module.context.ContextMapURLSerializer;
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
    private final UrlVariableSubstitutor urlVariableSubstitutor;
    private final ContextMapURLSerializer contextMapURLSerializer;
    private final WebFragmentHelper webFragmentHelper;

    @Autowired
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
    public WebItemModuleDescriptor createWebItemModuleDescriptor(String url, String linkId, boolean absolute, AddOnUrlContext addOnUrlContext)
    {
        return new RemoteConfluenceWebItemModuleDescriptor(urlVariableSubstitutor, contextMapURLSerializer, webFragmentHelper,
                url, linkId, absolute, addOnUrlContext);
    }

    private static final class RemoteConfluenceWebItemModuleDescriptor extends ConfluenceWebItemModuleDescriptor
    {
        private final UrlVariableSubstitutor urlVariableSubstitutor;
        private final ContextMapURLSerializer contextMapURLSerializer;
        private final WebFragmentHelper webFragmentHelper;
        private final String url;
        private final String linkId;
        private final boolean absolute;
        private final AddOnUrlContext addOnUrlContext;

        private RemoteConfluenceWebItemModuleDescriptor(
                UrlVariableSubstitutor urlVariableSubstitutor,
                ContextMapURLSerializer contextMapURLSerializer,
                WebFragmentHelper webFragmentHelper,
                String url,
                String linkId,
                boolean absolute, AddOnUrlContext addOnUrlContext)
        {
            this.urlVariableSubstitutor = urlVariableSubstitutor;
            this.contextMapURLSerializer = contextMapURLSerializer;
            this.webFragmentHelper = webFragmentHelper;
            this.url = url;
            this.linkId = linkId;
            this.absolute = absolute;
            this.addOnUrlContext = addOnUrlContext;
        }

        @Override
        public ConfluenceWebLink getLink()
        {
            return new ConfluenceWebLink(new RemoteWebLink(this, webFragmentHelper, urlVariableSubstitutor, contextMapURLSerializer, ,
                    url, linkId, absolute, addOnUrlContext));
        }
    }
}
