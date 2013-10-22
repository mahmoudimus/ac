package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemCapabilityBean;
import com.atlassian.plugin.connect.spi.module.DynamicMarkerCondition;
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
import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

@Component
public class WebItemModuleDescriptorFactory implements ConnectModuleDescriptorFactory<WebItemCapabilityBean,WebItemModuleDescriptor>
{
    private static final Logger log = LoggerFactory.getLogger(WebItemModuleDescriptorFactory.class);
    
    //TODO: rename this class to RemoteWebItemModuleDescriptorFactory
    private final com.atlassian.plugin.connect.plugin.module.webitem.WebItemModuleDescriptorFactory remoteWebItemDescriptorFactory;
    
    private final IconModuleFragmentFactory iconModuleFragmentFactory;

    @Autowired
    public WebItemModuleDescriptorFactory(com.atlassian.plugin.connect.plugin.module.webitem.WebItemModuleDescriptorFactory remoteWebItemDescriptorFactory, IconModuleFragmentFactory iconModuleFragmentFactory)
    {
        this.remoteWebItemDescriptorFactory = remoteWebItemDescriptorFactory;
        this.iconModuleFragmentFactory = iconModuleFragmentFactory;
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

        if(null != bean.getIcon() && !Strings.isNullOrEmpty(bean.getIcon().getUrl()))
        {
            webItemElement.add(iconModuleFragmentFactory.createFragment(plugin.getKey(), bean.getIcon()));
        }

        webItemElement.addElement("condition").addAttribute("class", DynamicMarkerCondition.class.getName());

        //TODO: implement condition beans and grab the condition from the bean. e.g. bean.getConditioon();
//        if (conditionClass != null)
//        {
//            webItemElement.addElement("condition").addAttribute("class", conditionClass.getName());
//        }
//
//        Condition condition = conditionProcessor.process(configurationElement, webItemElement, plugin.getKey());
//        
//        if (condition instanceof ContainingRemoteCondition)
//        {
//            styleClasses.add("remote-condition");
//            styleClasses.add("hidden");
//            styleClasses.add(conditionProcessor.createUniqueUrlHash(plugin.getKey(), ((ContainingRemoteCondition) condition).getConditionUrl()));
//        }

        if(!bean.getStyleClasses().isEmpty())
        {
            webItemElement.addElement("styleClass").setText(Joiner.on(" ").join(bean.getStyleClasses()));
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
        
        final WebItemModuleDescriptor descriptor = remoteWebItemDescriptorFactory.createWebItemModuleDescriptor(url, key, absolute);
        
        descriptor.init(plugin, webItemElement);
        
        return descriptor;
    }

}
