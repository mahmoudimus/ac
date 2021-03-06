package com.atlassian.plugin.connect.plugin.web.item;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.web.WebFragmentLocationQualifier;
import com.atlassian.plugin.connect.api.web.condition.ConditionModuleFragmentFactory;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebSectionModuleBean;
import com.atlassian.plugin.connect.spi.web.item.ProductSpecificWebSectionModuleDescriptorFactory;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.atlassian.plugin.web.descriptors.WebSectionModuleDescriptor;
import com.google.common.base.Strings;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.atlassian.plugin.connect.api.util.Dom4jUtils.printNode;

@ExportAsDevService(ConnectWebSectionModuleDescriptorFactory.class)
@Component
public class DefaultConnectWebSectionModuleDescriptorFactory implements ConnectWebSectionModuleDescriptorFactory {
    private static final Logger log = LoggerFactory.getLogger(ConnectWebSectionModuleDescriptorFactory.class);

    private final WebFragmentLocationQualifier webFragmentLocationQualifier;
    private final ConditionModuleFragmentFactory conditionModuleFragmentFactory;
    private final ProductSpecificWebSectionModuleDescriptorFactory webSectionModuleDescriptorFactory;

    @Autowired
    public DefaultConnectWebSectionModuleDescriptorFactory(
            WebFragmentLocationQualifier webFragmentLocationQualifier,
            ConditionModuleFragmentFactory conditionModuleFragmentFactory,
            ProductSpecificWebSectionModuleDescriptorFactory webSectionModuleDescriptorFactory) {
        this.webFragmentLocationQualifier = webFragmentLocationQualifier;
        this.conditionModuleFragmentFactory = conditionModuleFragmentFactory;
        this.webSectionModuleDescriptorFactory = webSectionModuleDescriptorFactory;
    }

    @Override
    public WebSectionModuleDescriptor createModuleDescriptor(WebSectionModuleBean bean, ConnectAddonBean connectAddonBean, Plugin plugin) {
        Element webSectionElement = new DOMElement("web-section");

        String webSectionKey = bean.getKey(connectAddonBean);
        String location = webFragmentLocationQualifier.processLocation(bean.getLocation(), connectAddonBean);
        String i18nKeyOrName = Strings.isNullOrEmpty(bean.getName().getI18n()) ? bean.getDisplayName() : bean.getName().getI18n();

        webSectionElement.addAttribute("key", webSectionKey);
        webSectionElement.addAttribute("location", location);
        webSectionElement.addAttribute("weight", Integer.toString(bean.getWeight()));
        webSectionElement.addAttribute("i18n-name-key", i18nKeyOrName);
        webSectionElement.addAttribute("name", bean.getDisplayName());

        webSectionElement.addElement("label")
                .addAttribute("key", bean.getName().getI18n())
                .setText(bean.getName().getValue());

        if (null != bean.getTooltip()) {
            webSectionElement.addElement("tooltip")
                    .addAttribute("key", bean.getTooltip().getI18n())
                    .setText(bean.getTooltip().getValue());
        }

        if (!bean.getConditions().isEmpty()) {
            webSectionElement.add(conditionModuleFragmentFactory.createFragment(connectAddonBean.getKey(), bean.getConditions()));
        }

        if (log.isDebugEnabled()) {
            log.debug("Created web section: " + printNode(webSectionElement));
        }

        return createWebSectionDescriptor(plugin, webSectionElement);
    }

    private WebSectionModuleDescriptor createWebSectionDescriptor(Plugin plugin, Element webSectionElement) {
        final WebSectionModuleDescriptor descriptor = webSectionModuleDescriptorFactory.createWebSectionModuleDescriptor();
        descriptor.init(plugin, webSectionElement);
        return descriptor;
    }

}
