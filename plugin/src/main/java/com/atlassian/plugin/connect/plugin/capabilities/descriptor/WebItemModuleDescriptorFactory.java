package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import java.util.List;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemCapabilityBean;
import com.atlassian.plugin.connect.plugin.module.webitem.ProductSpecificWebItemModuleDescriptorFactory;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.atlassian.plugin.connect.plugin.capabilities.util.ModuleKeyGenerator.nameToKey;
import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.*;
import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

@Component
public class WebItemModuleDescriptorFactory implements ConnectModuleDescriptorFactory<WebItemCapabilityBean,WebItemModuleDescriptor>
{
    private static final Logger log = LoggerFactory.getLogger(WebItemModuleDescriptorFactory.class);
    
    //TODO: rename this class to RemoteWebItemModuleDescriptorFactory
    private final ProductSpecificWebItemModuleDescriptorFactory productWebItemDescriptorFactory;
    
    private final IconModuleFragmentFactory iconModuleFragmentFactory;
    private final ConditionModuleFragmentFactory conditionModuleFragmentFactory;

    @Autowired
    public WebItemModuleDescriptorFactory(ProductSpecificWebItemModuleDescriptorFactory productWebItemDescriptorFactory, IconModuleFragmentFactory iconModuleFragmentFactory, ConditionModuleFragmentFactory conditionModuleFragmentFactory)
    {
        this.productWebItemDescriptorFactory = productWebItemDescriptorFactory;
        this.iconModuleFragmentFactory = iconModuleFragmentFactory;
        this.conditionModuleFragmentFactory = conditionModuleFragmentFactory;
    }

    @Override
    public WebItemModuleDescriptor createModuleDescriptor(Plugin plugin, BundleContext addonBundleContext, WebItemCapabilityBean bean)
    {
        Element webItemElement = new DOMElement("web-item");

        String webItemKey = bean.getKey();
        
        webItemElement.addAttribute("key", webItemKey);
        webItemElement.addAttribute("section",bean.getLocation());
        webItemElement.addAttribute("weight", Integer.toString(bean.getWeight()));

        webItemElement.addElement("label")
                      .addAttribute("key", escapeHtml(bean.getName().getI18n()))
                      .setText(escapeHtml(bean.getName().getValue()));

        Element linkElement = webItemElement.addElement("link").addAttribute("linkId", webItemKey);
        linkElement.setText(bean.getLink());

        List<String> styles = newArrayList(bean.getStyleClasses());
        
        if(null != bean.getIcon() && !Strings.isNullOrEmpty(bean.getIcon().getUrl()))
        {
            webItemElement.add(iconModuleFragmentFactory.createFragment(plugin.getKey(), bean.getIcon()));
        }

        if(!bean.getConditions().isEmpty())
        {
            webItemElement.add(conditionModuleFragmentFactory.createFragment(plugin.getKey(),bean.getConditions(),"#" + webItemKey));
        }

        if(!styles.isEmpty())
        {
            webItemElement.addElement("styleClass").setText(Joiner.on(" ").join(styles));
        }

        if (log.isDebugEnabled())
        {
            log.debug("Created web item: " + printNode(webItemElement));
        }

        return createWebItemDescriptor(plugin, webItemElement, webItemKey, bean.getLink(), bean.isAbsolute());
    }

    private WebItemModuleDescriptor createWebItemDescriptor(Plugin plugin, Element webItemElement, String key, String url, boolean absolute)
    {
        webItemElement.addAttribute("system", "true");
        
        final WebItemModuleDescriptor descriptor = productWebItemDescriptorFactory.createWebItemModuleDescriptor(url, key, absolute);
        
        descriptor.init(plugin, webItemElement);
        
        return descriptor;
    }

}
