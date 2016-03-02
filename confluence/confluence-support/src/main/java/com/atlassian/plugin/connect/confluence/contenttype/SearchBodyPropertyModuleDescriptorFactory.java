package com.atlassian.plugin.connect.confluence.contenttype;

import com.atlassian.confluence.plugin.descriptor.SearchBodyPropertyModuleDescriptor;
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
public class SearchBodyPropertyModuleDescriptorFactory
        implements ConnectModuleDescriptorFactory<ExtensibleContentTypeModuleBean, SearchBodyPropertyModuleDescriptor> {
    private static final Logger log = LoggerFactory.getLogger(SearchBodyPropertyModuleDescriptorFactory.class);

    private final ModuleFactory moduleFactory;

    @Autowired
    public SearchBodyPropertyModuleDescriptorFactory(ModuleFactory moduleFactory) {
        this.moduleFactory = moduleFactory;
    }

    @Override
    public SearchBodyPropertyModuleDescriptor createModuleDescriptor(ExtensibleContentTypeModuleBean bean, ConnectAddonBean addon, Plugin plugin) {
        String contentTypeName = bean.getContentTypeName(addon);
        String searchBodyPropertyModuleKey = bean.getSearchBodyPropertyModuleKey(addon);
        String contentPropertyKey = bean.getApiSupport().getIndexing().getContentPropertyBody();

        Element descriptionElement = new DOMElement("description");
        descriptionElement.addText("Search Body Content Property definition for Extensible Content Type: " + contentTypeName);

        Element extractorElement = new DOMElement("search-body-property");
        extractorElement.addAttribute("name", searchBodyPropertyModuleKey);
        extractorElement.addAttribute("key", searchBodyPropertyModuleKey);
        extractorElement.addAttribute("content-type", plugin.getKey() + ":" + contentTypeName);
        extractorElement.addAttribute("content-property", contentPropertyKey);
        extractorElement.add(descriptionElement);

        if (log.isDebugEnabled()) {
            log.debug(Dom4jUtils.printNode(extractorElement));
        }

        final SearchBodyPropertyModuleDescriptor descriptor = new SearchBodyPropertyModuleDescriptor(moduleFactory);
        descriptor.init(plugin, extractorElement);

        return descriptor;
    }
}
