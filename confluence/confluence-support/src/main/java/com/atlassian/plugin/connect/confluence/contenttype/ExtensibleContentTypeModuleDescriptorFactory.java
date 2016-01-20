package com.atlassian.plugin.connect.confluence.contenttype;

import com.atlassian.confluence.content.ContentEntityAdapter;
import com.atlassian.confluence.content.ContentTypeModuleDescriptor;
import com.atlassian.confluence.content.apisupport.ApiSupportProvider;
import com.atlassian.confluence.content.apisupport.CustomContentApiSupportParams;
import com.atlassian.confluence.security.PermissionDelegate;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.api.lifecycle.ConnectModuleDescriptorFactory;
import com.atlassian.plugin.connect.api.util.Dom4jUtils;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ExtensibleContentTypeModuleBean;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.module.PrefixDelegatingModuleFactory;
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
    private final ApiSupportProvider apiSupportProvider;
    private final ModuleFactory moduleFactory;

    @Autowired
    public ExtensibleContentTypeModuleDescriptorFactory(final ModuleFactory moduleFactory, final ApiSupportProvider apiSupportProvider)
    {
        this.apiSupportProvider = apiSupportProvider;
        this.moduleFactory = moduleFactory;
    }

    @Override
    public ContentTypeModuleDescriptor createModuleDescriptor(ExtensibleContentTypeModuleBean bean, ConnectAddonBean addon, Plugin plugin)
    {
        Element descriptionElement = new DOMElement("description");
        descriptionElement.addText("Support for Extensible Content Type for Atlassian Connect add-ons");

        Element contentTypeElement = new DOMElement("content-type");
        contentTypeElement.addAttribute("key", "extensible-" + bean.getRawKey());
        contentTypeElement.addAttribute("name", "extensible-" + bean.getRawKey());
        contentTypeElement.addAttribute("class", ExtensibleContentType.class.getTypeName());
        contentTypeElement.add(descriptionElement);

        if(log.isDebugEnabled()) {
            log.debug(Dom4jUtils.printNode(contentTypeElement));
        }

        final ContentTypeModuleDescriptor descriptor = new ContentTypeModuleDescriptor(moduleFactory, apiSupportProvider);
        descriptor.init(plugin, contentTypeElement);
        return descriptor;
    }
}
