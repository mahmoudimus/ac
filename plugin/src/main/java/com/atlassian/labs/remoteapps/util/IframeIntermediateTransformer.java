package com.atlassian.labs.remoteapps.util;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.elements.ResourceLocation;
import com.atlassian.plugin.servlet.DownloadableResource;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceUrlProvider;
import com.atlassian.plugin.webresource.transformer.CharSequenceDownloadableResource;
import com.atlassian.plugin.webresource.transformer.WebResourceTransformer;
import org.dom4j.Element;

public class IframeIntermediateTransformer implements WebResourceTransformer {
    private WebResourceUrlProvider webResourceUrlProvider;
    private ModuleDescriptor moduleDescriptor;

    public IframeIntermediateTransformer(WebResourceUrlProvider webResourceUrlProvider, ModuleDescriptor moduleDescriptor) {
        this.webResourceUrlProvider = webResourceUrlProvider;
        this.moduleDescriptor = moduleDescriptor;
    }

    @Override
    public DownloadableResource transform(Element element, ResourceLocation resourceLocation, String filePath,
                                          DownloadableResource origResource) {
        return null;
    }

    public class StaticHtmlDownloadResource extends CharSequenceDownloadableResource {

        public StaticHtmlDownloadResource(DownloadableResource originalResource) {
            super(originalResource);
        }

        @Override
        protected CharSequence transform(CharSequence originalContent) {
            return originalContent.toString().replace("@easyXdmUrl",
                    webResourceUrlProvider.getStaticPluginResourceUrl(moduleDescriptor.getCompleteKey(),
                            "easyXDM.js", UrlMode.AUTO));
        }

    }
}
