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
public class ContentPropertyChangeExtractorModuleDescriptorFactory
        implements ConnectModuleDescriptorFactory<ExtensibleContentTypeModuleBean, ContentPropertyExtractorModuleDescriptor>
{
    private static final Logger log = LoggerFactory.getLogger(ContentPropertyChangeExtractorModuleDescriptorFactory.class);

    private final ModuleFactory moduleFactory;
    private final ContentPropertyService contentPropertyService;
    private final ConfluenceFeatureManager confluenceFeatureManager;

    @Autowired
    public ContentPropertyChangeExtractorModuleDescriptorFactory(
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
        String completeContentTypeKey = ExtensibleContentTypeUtils.getCompleteContentType(addon, bean);
        String extractorKey = ExtensibleContentTypeUtils.getChangeExtractorKey(addon, bean);
        String contentPropertyKey = bean.getApiSupport().getIndexing().getContentPropertyBody();

        Element changeExtractorElement = new DOMElement("change-extractor");
        changeExtractorElement.addAttribute("name", "Content Property Change Extractor for Extensible Content Type " + completeContentTypeKey);
        changeExtractorElement.addAttribute("key", extractorKey);
        changeExtractorElement.addAttribute("class", ContentPropertyExtractor.class.getName());
        changeExtractorElement.addAttribute("requires-latest-version", "true");
        changeExtractorElement.addAttribute("priority", "800");

        if (ContentPropertyChangeExtractorModuleDescriptorFactory.log.isDebugEnabled())
        {
            ContentPropertyChangeExtractorModuleDescriptorFactory.log.debug(Dom4jUtils.printNode(changeExtractorElement));
        }

        final ContentPropertyExtractorModuleDescriptor descriptor =
                new ContentPropertyExtractorModuleDescriptor(
                        moduleFactory,
                        confluenceFeatureManager,
                        contentPropertyService,
                        contentPropertyKey);
        descriptor.init(plugin, changeExtractorElement);

        return descriptor;
    }
}
