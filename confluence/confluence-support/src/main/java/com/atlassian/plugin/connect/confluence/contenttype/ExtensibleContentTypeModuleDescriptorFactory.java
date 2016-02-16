package com.atlassian.plugin.connect.confluence.contenttype;

import com.atlassian.confluence.api.impl.service.content.factory.ContentFactory;
import com.atlassian.confluence.content.ContentTypeModuleDescriptor;
import com.atlassian.confluence.content.CustomContentManager;
import com.atlassian.confluence.content.apisupport.ApiSupportProvider;
import com.atlassian.confluence.content.apisupport.CustomContentApiSupportParams;
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
public class ExtensibleContentTypeModuleDescriptorFactory implements ConnectModuleDescriptorFactory<ExtensibleContentTypeModuleBean, ContentTypeModuleDescriptor>
{
    private static final Logger log = LoggerFactory.getLogger(ExtensibleContentTypeModuleDescriptorFactory.class);
    private final ContentFactory contentFactory;
    private final ModuleFactory moduleFactory;
    private final ContentTypeMapper contentTypeMapper;
    private final ApiSupportProvider apiSupportProvider;
    private final CustomContentManager customContentManager;
    private final CustomContentApiSupportParams customContentApiSupportParams;

    @Autowired
    public ExtensibleContentTypeModuleDescriptorFactory(
            ContentFactory contentFactory,
            ModuleFactory moduleFactory,
            ContentTypeMapper contentTypeMapper,
            ApiSupportProvider apiSupportProvider,
            CustomContentManager customContentManager,
            CustomContentApiSupportParams customContentApiSupportParams)
    {
        this.contentFactory = contentFactory;
        this.moduleFactory = moduleFactory;
        this.contentTypeMapper = contentTypeMapper;
        this.customContentManager = customContentManager;
        this.customContentApiSupportParams = customContentApiSupportParams;
        this.apiSupportProvider = apiSupportProvider;
    }

    @Override
    public ContentTypeModuleDescriptor createModuleDescriptor(ExtensibleContentTypeModuleBean bean, ConnectAddonBean addon, Plugin plugin)
    {
        String contentTypeKey = ExtensibleUtils.getContentType(addon, bean);
        String completeModuleKey = ExtensibleUtils.getCompleteModuleKey(addon, bean);

        Element descriptionElement = new DOMElement("description");
        descriptionElement.addText("Support for Extensible Content Type for Atlassian Connect add-ons");

        Element contentTypeElement = new DOMElement("content-type");
        contentTypeElement.addAttribute("key", contentTypeKey);
        contentTypeElement.addAttribute("name", contentTypeKey);
        contentTypeElement.addAttribute("class", ExtensibleContentType.class.getName());
        contentTypeElement.add(descriptionElement);

        Element webResourceElement = new DOMElement("web-resource");


        if(log.isDebugEnabled()) {
            log.debug(Dom4jUtils.printNode(contentTypeElement));
        }

        final ExtensibleContentTypeModuleDescriptor descriptor =
                new ExtensibleContentTypeModuleDescriptor(
                        contentTypeKey,
                        completeModuleKey,
                        bean,
                        moduleFactory,
                        contentFactory,
                        contentTypeMapper,
                        apiSupportProvider,
                        customContentManager,
                        customContentApiSupportParams);
        descriptor.init(plugin, contentTypeElement);
        return descriptor;
    }
}
