package com.atlassian.plugin.connect.confluence.contenttype;

import com.atlassian.confluence.api.service.content.ContentService;
import com.atlassian.confluence.api.service.pagination.PaginationService;
import com.atlassian.confluence.content.ContentTypeModuleDescriptor;
import com.atlassian.confluence.content.CustomContentManager;
import com.atlassian.confluence.content.apisupport.ApiSupportProvider;
import com.atlassian.confluence.content.apisupport.CustomContentApiSupportParams;
import com.atlassian.confluence.security.PermissionManager;
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
public class ExtensibleContentTypeModuleDescriptorFactory
        implements ConnectModuleDescriptorFactory<ExtensibleContentTypeModuleBean, ContentTypeModuleDescriptor> {
    private static final Logger log = LoggerFactory.getLogger(ExtensibleContentTypeModuleDescriptorFactory.class);

    private final ModuleFactory moduleFactory;
    private final PermissionManager permissionManager;
    private final CustomContentApiSupportParams customContentApiSupportParams;

    @Autowired
    public ExtensibleContentTypeModuleDescriptorFactory(
            ModuleFactory moduleFactory,
            PermissionManager permissionManager,
            CustomContentApiSupportParams customContentApiSupportParams) {

        this.moduleFactory = moduleFactory;
        this.customContentApiSupportParams = customContentApiSupportParams;
        this.permissionManager = permissionManager;
    }

    @Override
    public ContentTypeModuleDescriptor createModuleDescriptor(ExtensibleContentTypeModuleBean bean, ConnectAddonBean addon, Plugin plugin) {
        String contentTypeName = bean.getContentTypeName(addon);
        String description = String.format("Support for Extensible Content Type: %s", contentTypeName);

        Element descriptionElement = new DOMElement("description");
        descriptionElement.addText(description);

        Element contentTypeElement = new DOMElement("content-type");
        contentTypeElement.addAttribute("key", contentTypeName);
        contentTypeElement.addAttribute("name", contentTypeName);
        contentTypeElement.addAttribute("class", ExtensibleContentType.class.getName());
        contentTypeElement.add(descriptionElement);

        if (log.isDebugEnabled()) {
            log.debug(Dom4jUtils.printNode(contentTypeElement));
        }

        final ExtensibleContentTypeModuleDescriptor descriptor =
                new ExtensibleContentTypeModuleDescriptor(
                        bean,
                        moduleFactory,
                        permissionManager,
                        customContentApiSupportParams);
        descriptor.init(plugin, contentTypeElement);

        return descriptor;
    }
}
