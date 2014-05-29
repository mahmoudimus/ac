package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.confluence.plugins.createcontent.extensions.BlueprintModuleDescriptor;
import com.atlassian.confluence.util.i18n.DocumentationBeanFactory;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.BlueprintModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.builder.LinkBeanBuilder;
import com.atlassian.plugin.connect.spi.util.Dom4jUtils;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.google.common.base.Strings;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.StringWriter;

/**
 * The {@link com.atlassian.plugin.connect.modules.beans.BlueprintModuleBean} to
 * {@link com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor} part of the blueprint
 * mapping.
 *
 * @see com.atlassian.plugin.connect.plugin.capabilities.descriptor.BlueprintModuleDescriptorFactory
 * @see com.atlassian.plugin.connect.plugin.capabilities.descriptor.BlueprintContentTemplateModuleDescriptorFactory
 */
@ConfluenceComponent
public class BlueprintModuleDescriptorFactory
        implements ConnectModuleDescriptorFactory<BlueprintModuleBean, BlueprintModuleDescriptor>
{
    private static final Logger log = LoggerFactory.getLogger(ConnectModuleDescriptorFactory.class);
    private final ModuleFactory moduleFactory;

    @Autowired
    public BlueprintModuleDescriptorFactory(ModuleFactory moduleFactory) {
        this.moduleFactory = moduleFactory;
    }

    @Override
    public BlueprintModuleDescriptor createModuleDescriptor(ConnectAddonBean addon, Plugin plugin, BlueprintModuleBean bean) {
        DocumentationBeanFactory documentationBeanFactory = new ConnectDocumentationBeanFactory(new LinkBeanBuilder().build());

        Element blueprintElement = new DOMElement("blueprint");

        String blueprintKey = BlueprintUtils.getBlueprintKey(addon, bean);
        String contentTemplateKey = BlueprintUtils.getContentTemplateKey(addon, bean);

        String i18nKeyOrName = Strings.isNullOrEmpty(bean.getName().getI18n()) ? bean.getDisplayName() : bean.getName().getI18n();
        blueprintElement.addAttribute("key", blueprintKey);
        blueprintElement.addAttribute("section", "system.create.dialog/content");
        blueprintElement.addAttribute("i18n-name-key", i18nKeyOrName);
        blueprintElement.addAttribute("index-key", bean.getRawKey());
        blueprintElement.addAttribute("content-template-key",contentTemplateKey);
        blueprintElement.addAttribute("system", "true");

        if(log.isDebugEnabled())
            log.debug(Dom4jUtils.printNode(blueprintElement));

        final BlueprintModuleDescriptor descriptor = new BlueprintModuleDescriptor(moduleFactory, documentationBeanFactory);
        descriptor.init(plugin, blueprintElement);
        return descriptor;
    }
}
