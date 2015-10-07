package com.atlassian.plugin.connect.plugin.module.webitem;

import com.atlassian.plugin.connect.api.iframe.context.ModuleContextFilter;
import com.atlassian.plugin.connect.api.iframe.render.uri.IFrameUriBuilderFactory;
import com.atlassian.plugin.connect.api.iframe.webpanel.PluggableParametersExtractor;
import com.atlassian.plugin.connect.api.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.api.module.webitem.WebItemModuleDescriptorData;
import com.atlassian.plugin.connect.api.module.webitem.WebLinkFactory;
import com.atlassian.plugin.web.WebFragmentHelper;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.plugin.web.model.WebLink;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WebLinkFactoryImpl implements WebLinkFactory
{
    private final PluggableParametersExtractor webFragmentModuleContextExtractor;
    private final IFrameUriBuilderFactory iFrameUriBuilderFactory;
    private final WebFragmentHelper webFragmentHelper;
    private final UrlVariableSubstitutor urlVariableSubstitutor;
    private final ModuleContextFilter moduleContextFilter;

    @Autowired
    public WebLinkFactoryImpl(PluggableParametersExtractor webFragmentModuleContextExtractor,
            IFrameUriBuilderFactory iFrameUriBuilderFactory,
            WebFragmentHelper webFragmentHelper,
            UrlVariableSubstitutor urlVariableSubstitutor,
            ModuleContextFilter moduleContextFilter)
    {
        this.webFragmentModuleContextExtractor = webFragmentModuleContextExtractor;
        this.iFrameUriBuilderFactory = iFrameUriBuilderFactory;
        this.webFragmentHelper = webFragmentHelper;
        this.urlVariableSubstitutor = urlVariableSubstitutor;
        this.moduleContextFilter = moduleContextFilter;
    }

    @Override
    public WebLink createRemoteWebLink(WebItemModuleDescriptor remoteWebItemModuleDescriptor, WebItemModuleDescriptorData data)
    {
        return new RemoteWebLink(remoteWebItemModuleDescriptor, webFragmentHelper, iFrameUriBuilderFactory, urlVariableSubstitutor,
                webFragmentModuleContextExtractor, moduleContextFilter, data.getUrl(), data.getUrl(),
                data.getModuleKey(), data.isAbsolute(), data.getAddOnUrlContext(), data.isDialog());
    }
}
