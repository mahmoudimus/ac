package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.BlueprintModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.module.webitem.ProductSpecificWebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.util.Dom4jUtils;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.google.common.base.Strings;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The {@link com.atlassian.plugin.connect.modules.beans.BlueprintModuleBean} to
 * {@link com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor} part of the blueprint
 * mapping.
 *
 * @see com.atlassian.plugin.connect.plugin.capabilities.descriptor.BlueprintModuleDescriptorFactory
 * @see com.atlassian.plugin.connect.plugin.capabilities.descriptor.BlueprintContentTemplateModuleDescriptorFactory
 */
@ConfluenceComponent
public class BlueprintWebItemModuleDescriptorFactory
        implements ConnectModuleDescriptorFactory<BlueprintModuleBean, WebItemModuleDescriptor> {

    private static final Logger log = LoggerFactory.getLogger(BlueprintWebItemModuleDescriptorFactory.class);

    private final ProductSpecificWebItemModuleDescriptorFactory productWebItemDescriptorFactory;

    @Autowired
    public BlueprintWebItemModuleDescriptorFactory(ProductSpecificWebItemModuleDescriptorFactory productWebItemDescriptorFactory)
    {
        this.productWebItemDescriptorFactory = productWebItemDescriptorFactory;
    }

    @Override
    public WebItemModuleDescriptor createModuleDescriptor(ConnectAddonBean addon, Plugin plugin, BlueprintModuleBean bean) {
        Element webItemElement = new DOMElement("web-item");

        String webItemKey = BlueprintUtils.getWebItemKey(addon, bean);
        String blueprintKey = BlueprintUtils.getBlueprintKey(addon, bean);

        String i18nKeyOrName = Strings.isNullOrEmpty(bean.getName().getI18n()) ? bean.getDisplayName() : bean.getName().getI18n();
        webItemElement.addAttribute("key", webItemKey);
        webItemElement.addAttribute("section", "system.create.dialog/content");
        webItemElement.addAttribute("i18n-name-key", i18nKeyOrName);

        webItemElement.addElement("resource")
                .addAttribute("name", "icon")
                .addAttribute("type", "download")
                .addAttribute("location", "web-item-icon-resource-location"); // TODO

        webItemElement.addElement("param")
                .addAttribute("name","blueprintKey")
                .addAttribute("value", blueprintKey);

        webItemElement.addAttribute("system", "true");

//        if(log.isDebugEnabled())
        log.info(Dom4jUtils.printNode(webItemElement));

        final WebItemModuleDescriptor descriptor = productWebItemDescriptorFactory
                .createWebItemModuleDescriptor(null, addon.getKey(), webItemKey, true, null, true);
        descriptor.init(plugin, webItemElement);
        return descriptor;
    }

}
