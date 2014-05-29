package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.confluence.languages.DefaultLocaleManager;
import com.atlassian.confluence.plugins.createcontent.extensions.ContentTemplateModuleDescriptor;
import com.atlassian.confluence.util.i18n.I18NBeanFactory;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.BlueprintModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.spi.util.Dom4jUtils;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
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
 * @see com.atlassian.plugin.connect.plugin.capabilities.descriptor.BlueprintWebItemModuleDescriptorFactory
 */
@ConfluenceComponent
public class BlueprintContentTemplateModuleDescriptorFactory
        implements ConnectModuleDescriptorFactory<BlueprintModuleBean, ContentTemplateModuleDescriptor> {

    private static final Logger log = LoggerFactory.getLogger(BlueprintContentTemplateModuleDescriptorFactory.class);

    private final ModuleFactory moduleFactory;
    private final I18NBeanFactory i18nBeanFactory;

    @Autowired
    public BlueprintContentTemplateModuleDescriptorFactory(ModuleFactory moduleFactory, I18NBeanFactory i18nBeanFactory) {
        this.moduleFactory = moduleFactory;
        this.i18nBeanFactory = i18nBeanFactory;
    }

    @Override
    public ContentTemplateModuleDescriptor createModuleDescriptor(ConnectAddonBean addon, Plugin plugin, BlueprintModuleBean bean) {
        Element contentTemplateElement = new DOMElement("content-template");

        String contentTemplateKey = bean.getKey(addon) + "-content-template";

        String i18nKeyOrName = Strings.isNullOrEmpty(bean.getName().getI18n()) ? bean.getDisplayName() : bean.getName().getI18n();
        contentTemplateElement.addAttribute("key", contentTemplateKey);
        contentTemplateElement.addAttribute("i18n-name-key", i18nKeyOrName);

        contentTemplateElement.addElement("resource")
                .addAttribute("name", "template")
                .addAttribute("type", "download")
                .addAttribute("location", addon.getBaseUrl() + bean.getBlueprintTemplate().getUrl());

        if (log.isDebugEnabled())
            log.debug(Dom4jUtils.printNode(contentTemplateElement));

        final ContentTemplateModuleDescriptor descriptor = new ContentTemplateModuleDescriptor(moduleFactory, i18nBeanFactory, new DefaultLocaleManager());
        descriptor.init(plugin, contentTemplateElement);
        return descriptor;
    }

}
