package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemModuleBean;
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

import java.util.List;

import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.printNode;
import static com.google.common.collect.Lists.newArrayList;


@Component
public class WebItemModuleDescriptorFactory implements ConnectModuleDescriptorFactory<WebItemModuleBean,WebItemModuleDescriptor>
{
    private static final Logger log = LoggerFactory.getLogger(WebItemModuleDescriptorFactory.class);
    
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
    public WebItemModuleDescriptor createModuleDescriptor(Plugin plugin, BundleContext addonBundleContext, WebItemModuleBean bean)
    {
        Element webItemElement = new DOMElement("web-item");

        String webItemKey = bean.getKey();
        
        webItemElement.addAttribute("key", webItemKey);
        webItemElement.addAttribute("section",bean.getLocation());
        webItemElement.addAttribute("weight", Integer.toString(bean.getWeight()));

        webItemElement.addElement("label")
                      .addAttribute("key", bean.getName().getI18n())
                      .setText(bean.getName().getValue());

        Element linkElement = webItemElement.addElement("link").addAttribute("linkId", webItemKey);
        linkElement.setText(bean.getLink());

        List<String> styles = newArrayList(bean.getStyleClasses());
        
        if(null != bean.getIcon())
        {
            webItemElement.add(iconModuleFragmentFactory.createFragment(plugin.getKey(), bean.getIcon()));
        }

        if(!bean.getConditions().isEmpty())
        {
            webItemElement.add(conditionModuleFragmentFactory.createFragment(plugin.getKey(),bean.getConditions(),"#" + webItemKey));
        }

        if(bean.getTarget().isDialogTarget())
        {
            styles.add("ap-dialog");
        }
        else if (bean.getTarget().isInlineDialogTarget())
        {
            styles.add("ap-inline-dialog");
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
