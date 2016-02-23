package com.atlassian.plugin.connect.confluence.web;

import com.atlassian.confluence.plugin.descriptor.web.descriptors.ConfluenceWebItemModuleDescriptor;
import com.atlassian.confluence.plugin.descriptor.web.model.ConfluenceWebLink;
import com.atlassian.plugin.connect.api.web.PluggableParametersExtractor;
import com.atlassian.plugin.connect.api.web.RemoteWebLink;
import com.atlassian.plugin.connect.api.web.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.api.web.context.ModuleContextFilter;
import com.atlassian.plugin.connect.api.web.iframe.ConnectUriFactory;
import com.atlassian.plugin.connect.modules.beans.AddonUrlContext;
import com.atlassian.plugin.connect.spi.web.item.ProductSpecificWebItemModuleDescriptorFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.plugin.web.WebFragmentHelper;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.sal.api.component.ComponentLocator;
import org.springframework.beans.factory.annotation.Autowired;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Creates ConfluenceWebItemModuleDescriptor with link pointing to remote plugin.
 */
@ConfluenceComponent
public class ConfluenceWebItemModuleDescriptorFactory implements ProductSpecificWebItemModuleDescriptorFactory {
    private final ConnectUriFactory connectUriFactory;
    private final PluggableParametersExtractor webFragmentModuleContextExtractor;
    private final ModuleContextFilter moduleContextFilter;
    private final UrlVariableSubstitutor urlVariableSubstitutor;

    @Autowired
    public ConfluenceWebItemModuleDescriptorFactory(
            ConnectUriFactory connectUriFactory,
            PluggableParametersExtractor webFragmentModuleContextExtractor,
            ModuleContextFilter moduleContextFilter,
            UrlVariableSubstitutor urlVariableSubstitutor) {
        this.urlVariableSubstitutor = checkNotNull(urlVariableSubstitutor);
        this.connectUriFactory = checkNotNull(connectUriFactory);
        this.webFragmentModuleContextExtractor = checkNotNull(webFragmentModuleContextExtractor);
        this.moduleContextFilter = checkNotNull(moduleContextFilter);
    }

    @Override
    public WebItemModuleDescriptor createWebItemModuleDescriptor(final String url, final String pluginKey, final String moduleKey, final boolean absolute, final AddonUrlContext addonUrlContext, final boolean isDialog, final String section) {
        WebFragmentHelper webFragmentHelper = ComponentLocator.getComponent(WebFragmentHelper.class);
        return new RemoteConfluenceWebItemModuleDescriptor(
                webFragmentHelper
                , connectUriFactory
                , webFragmentModuleContextExtractor
                , moduleContextFilter
                , urlVariableSubstitutor
                , url
                , pluginKey
                , moduleKey
                , absolute
                , addonUrlContext
                , isDialog);
    }

    private static final class RemoteConfluenceWebItemModuleDescriptor extends ConfluenceWebItemModuleDescriptor {
        private final WebFragmentHelper webFragmentHelper;
        private final ConnectUriFactory connectUriFactory;
        private final PluggableParametersExtractor webFragmentModuleContextExtractor;
        private final ModuleContextFilter moduleContextFilter;
        private final String url;
        private final UrlVariableSubstitutor urlVariableSubstitutor;
        private final String pluginKey;
        private final String moduleKey;
        private final boolean absolute;
        private final AddonUrlContext addonUrlContext;
        private final boolean isDialog;

        private RemoteConfluenceWebItemModuleDescriptor(
                WebFragmentHelper webFragmentHelper,
                ConnectUriFactory connectUriFactory,
                PluggableParametersExtractor webFragmentModuleContextExtractor, ModuleContextFilter moduleContextFilter,
                UrlVariableSubstitutor urlVariableSubstitutor, String url, String pluginKey, String moduleKey, boolean absolute,
                AddonUrlContext addonUrlContext, boolean isDialog) {
            this.connectUriFactory = connectUriFactory;
            this.webFragmentModuleContextExtractor = webFragmentModuleContextExtractor;
            this.moduleContextFilter = moduleContextFilter;
            this.urlVariableSubstitutor = urlVariableSubstitutor;
            this.pluginKey = pluginKey;
            this.moduleKey = moduleKey;
            this.webFragmentHelper = webFragmentHelper;
            this.url = url;
            this.absolute = absolute;
            this.addonUrlContext = addonUrlContext;
            this.isDialog = isDialog;
        }

        @Override
        public ConfluenceWebLink getLink() {
            return new ConfluenceWebLink(new RemoteWebLink(this, webFragmentHelper, connectUriFactory, urlVariableSubstitutor,
                    webFragmentModuleContextExtractor, moduleContextFilter, url, pluginKey, moduleKey, absolute, addonUrlContext, isDialog));
        }

        @Override
        public void destroy() {

        }
    }
}
