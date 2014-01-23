package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.WebSectionModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectAutowireUtil;
import com.atlassian.plugin.connect.plugin.module.websection.ProductSpecificWebSectionModuleDescriptorFactory;
import com.atlassian.plugin.web.descriptors.WebSectionModuleDescriptor;
import com.google.common.base.Strings;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.printNode;


@Component
public class ConnectWebSectionModuleDescriptorFactory implements ConnectModuleDescriptorFactory<WebSectionModuleBean, WebSectionModuleDescriptor>
{
    private static final Logger log = LoggerFactory.getLogger(ConnectWebSectionModuleDescriptorFactory.class);

    private final ConnectAutowireUtil connectAutowireUtil;
    private final ConditionModuleFragmentFactory conditionModuleFragmentFactory;
    private final ProductSpecificWebSectionModuleDescriptorFactory webSectionModuleDescriptorFactory;

    @Autowired
    public ConnectWebSectionModuleDescriptorFactory(final ConnectAutowireUtil connectAutowireUtil, ConditionModuleFragmentFactory conditionModuleFragmentFactory, final ProductSpecificWebSectionModuleDescriptorFactory webSectionModuleDescriptorFactory)
    {
        this.connectAutowireUtil = connectAutowireUtil;
        this.conditionModuleFragmentFactory = conditionModuleFragmentFactory;
        this.webSectionModuleDescriptorFactory = webSectionModuleDescriptorFactory;
    }

    @Override
    public WebSectionModuleDescriptor createModuleDescriptor(Plugin plugin, BundleContext addonBundleContext, WebSectionModuleBean bean)
    {
        Element webSectionElement = new DOMElement("web-section");

        String webSectionKey = bean.getKey();
        String i18nKeyOrName = Strings.isNullOrEmpty(bean.getName().getI18n()) ? bean.getDisplayName() : bean.getName().getI18n();

        webSectionElement.addAttribute("key", webSectionKey);
        webSectionElement.addAttribute("location", bean.getLocation());
        webSectionElement.addAttribute("weight", Integer.toString(bean.getWeight()));
        webSectionElement.addAttribute("i18n-name-key", i18nKeyOrName);

        webSectionElement.addElement("label")
                .addAttribute("key", bean.getName().getI18n())
                .setText(bean.getName().getValue());

        if (null != bean.getTooltip())
        {
            webSectionElement.addElement("tooltip")
                    .addAttribute("key", bean.getTooltip().getI18n())
                    .setText(bean.getTooltip().getValue());
        }

        if (!bean.getConditions().isEmpty())
        {
            webSectionElement.add(conditionModuleFragmentFactory.createFragment(plugin.getKey(), bean.getConditions(), "#" + webSectionKey));
        }

        if (log.isDebugEnabled())
        {
            log.debug("Created web section: " + printNode(webSectionElement));
        }

        return createWebSectionDescriptor(plugin, webSectionElement);
    }

    private WebSectionModuleDescriptor createWebSectionDescriptor(Plugin plugin, Element webSectionElement)
    {
        // is this needed?
        // webSectionElement.addAttribute("system", "true");

        final WebSectionModuleDescriptor descriptor = webSectionModuleDescriptorFactory.createWebSectionModuleDescriptor();
        descriptor.init(plugin, webSectionElement);
        return descriptor;
    }

}
