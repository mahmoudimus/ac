package com.atlassian.plugin.connect.confluence.blueprint;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.util.Dom4jUtils;
import com.atlassian.plugin.connect.modules.beans.BlueprintModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.api.lifecycle.ConnectModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.web.item.ProductSpecificWebItemModuleDescriptorFactory;
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
 * @see BlueprintModuleDescriptorFactory
 * @see BlueprintContentTemplateModuleDescriptorFactory
 */
@ConfluenceComponent
public class BlueprintWebItemModuleDescriptorFactory
        implements ConnectModuleDescriptorFactory<BlueprintModuleBean, WebItemModuleDescriptor>
{

    private static final Logger log = LoggerFactory.getLogger(BlueprintWebItemModuleDescriptorFactory.class);

    private final ProductSpecificWebItemModuleDescriptorFactory productWebItemDescriptorFactory;

    @Autowired
    public BlueprintWebItemModuleDescriptorFactory(ProductSpecificWebItemModuleDescriptorFactory productWebItemDescriptorFactory)
    {
        this.productWebItemDescriptorFactory = productWebItemDescriptorFactory;
    }

    @Override
    public WebItemModuleDescriptor createModuleDescriptor(BlueprintModuleBean bean, ConnectAddonBean addon, Plugin plugin)
    {
        Element webItemElement = new DOMElement("web-item");

        String webItemKey = BlueprintUtils.getWebItemKey(addon, bean);
        String blueprintKey = BlueprintUtils.getBlueprintKey(addon, bean);

        String i18nKeyOrName = Strings.isNullOrEmpty(bean.getName().getI18n()) ? bean.getDisplayName() : bean.getName().getI18n();
        webItemElement.addAttribute("key", webItemKey);

        String section = "system.create.dialog/content";
        webItemElement.addAttribute("section", section);

        webItemElement.addAttribute("i18n-name-key", i18nKeyOrName);

//      See: https://ecosystem.atlassian.net/browse/CE-19
//      Doesn't quite work yet, in the mean time don't create an icon entry so you at least get the default icon
//      instead of a broken image
//        webItemElement.addElement("resource")
//                .addAttribute("name", "icon")
//                .addAttribute("type", "download")
//                .addAttribute("location", addon.getBaseUrl() + bean.getIcon().getUrl());

        webItemElement.addElement("param")
                .addAttribute("name", "blueprintKey")
                .addAttribute("value", blueprintKey);

        webItemElement.addAttribute("system", "true");

        if (log.isDebugEnabled())
        {
            log.debug(Dom4jUtils.printNode(webItemElement));
        }

        final WebItemModuleDescriptor descriptor = productWebItemDescriptorFactory
                .createWebItemModuleDescriptor(null, addon.getKey(), webItemKey, true, null, true, section);
        descriptor.init(plugin, webItemElement);
        return descriptor;
    }

}
