package com.atlassian.plugin.connect.confluence.contenttype;

import com.atlassian.confluence.api.service.content.ContentPropertyService;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.lifecycle.ConnectModuleDescriptorFactory;
import com.atlassian.plugin.connect.api.util.Dom4jUtils;
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

    @Autowired
    public ContentPropertyChangeExtractorModuleDescriptorFactory(ModuleFactory moduleFactory, ContentPropertyService contentPropertyService)
    {
        this.moduleFactory = moduleFactory;
        this.contentPropertyService = contentPropertyService;
    }

    @Override
    public ContentPropertyExtractorModuleDescriptor createModuleDescriptor(ExtensibleContentTypeModuleBean bean, ConnectAddonBean addon, Plugin plugin)
    {
        String contentPropertyKey = bean.getApiSupport().getIndexing().getContentPropertyBody();

        Element changeExtractorElement = new DOMElement("change-extractor");
        changeExtractorElement.addAttribute("name", "Content Property Change Extractor for Extensible Content Type");
        changeExtractorElement.addAttribute("key", ContentPropertyExtractor.class.getSimpleName());
        changeExtractorElement.addAttribute("class", ContentPropertyExtractor.class.getName());
        changeExtractorElement.addAttribute("requires-latest-version", "true");
        changeExtractorElement.addAttribute("priority", "800");

        if (ContentPropertyChangeExtractorModuleDescriptorFactory.log.isDebugEnabled())
        {
            ContentPropertyChangeExtractorModuleDescriptorFactory.log.debug(Dom4jUtils.printNode(changeExtractorElement));
        }

        final ContentPropertyExtractorModuleDescriptor descriptor = new ContentPropertyExtractorModuleDescriptor(moduleFactory, contentPropertyService, contentPropertyKey);
        descriptor.init(plugin, changeExtractorElement);

        return descriptor;
    }
}
