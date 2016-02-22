package com.atlassian.plugin.connect.confluence.contenttype;

import com.atlassian.confluence.api.service.content.ContentPropertyService;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.lifecycle.ConnectModuleDescriptorFactory;
import com.atlassian.plugin.connect.api.util.Dom4jUtils;
import com.atlassian.plugin.connect.confluence.ConfluenceFeatureManager;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ExtensibleContentTypeModuleBean;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;

import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@ConfluenceComponent
public class ContentPropertyExtractorModuleDescriptorFactory
        implements ConnectModuleDescriptorFactory<ExtensibleContentTypeModuleBean, ContentPropertyExtractorModuleDescriptor>
{
    private static final Logger log = LoggerFactory.getLogger(ContentPropertyExtractorModuleDescriptorFactory.class);

    private final ModuleFactory moduleFactory;
    private final ConfluenceFeatureManager confluenceFeatureManager;
    private final ContentPropertyService contentPropertyService;

    @Autowired
    public ContentPropertyExtractorModuleDescriptorFactory(
            ModuleFactory moduleFactory,
            ConfluenceFeatureManager confluenceFeatureManager,
            ContentPropertyService contentPropertyService)
    {
        this.moduleFactory = moduleFactory;
        this.confluenceFeatureManager = confluenceFeatureManager;
        this.contentPropertyService = contentPropertyService;
    }

    @Override
    public ContentPropertyExtractorModuleDescriptor createModuleDescriptor(ExtensibleContentTypeModuleBean bean, ConnectAddonBean addon, Plugin plugin)
    {
        String contentTypeKey = ExtensibleContentTypeUtils.getContentType(addon, bean);
        String contentPropertyKey = bean.getApiSupport().getIndexing().getContentPropertyBody();

        Element descriptionElement = new DOMElement("description");
        descriptionElement.addText(String.format("Indexes content property %s for Extensible Content Type %s", contentPropertyKey, contentTypeKey));

        Element extractorElement = new DOMElement("extractor");
        extractorElement.addAttribute("name", "Content Property Extractor for Extensible Content Type " + contentTypeKey);
        extractorElement.addAttribute("key", "extensibleContentTypeExtractor-" + contentTypeKey);
        extractorElement.addAttribute("class", ContentPropertyExtractor.class.getName());
        extractorElement.addAttribute("requires-latest-version", "true");
        extractorElement.addAttribute("priority", "800");
        extractorElement.add(descriptionElement);

        if (ContentPropertyExtractorModuleDescriptorFactory.log.isDebugEnabled())
        {
            ContentPropertyExtractorModuleDescriptorFactory.log.debug(Dom4jUtils.printNode(extractorElement));
        }

        final ContentPropertyExtractorModuleDescriptor descriptor =
                new ContentPropertyExtractorModuleDescriptor(
                        moduleFactory,
                        confluenceFeatureManager,
                        contentPropertyService,
                        contentPropertyKey);
        descriptor.init(plugin, extractorElement);

        return descriptor;
    }
}
