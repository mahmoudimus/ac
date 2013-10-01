package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.IconCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemCapabilityBean;
import com.atlassian.plugin.connect.plugin.module.ContainingRemoteCondition;
import com.atlassian.plugin.connect.spi.module.DynamicMarkerCondition;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.conditions.AlwaysDisplayCondition;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.uri.Uri;
import com.atlassian.uri.UriBuilder;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.atlassian.plugin.connect.plugin.module.util.redirect.RedirectServlet.getPermanentRedirectUrl;
import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.*;
import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

@Component
public class WebItemModuleDescriptorFactory
{
    private static final Logger log = LoggerFactory.getLogger(WebItemModuleDescriptorFactory.class);
    
    //TODO: rename this class to RemoteWebItemModuleDescriptorFactory
    private final com.atlassian.plugin.connect.plugin.module.webitem.WebItemModuleDescriptorFactory webItemModuleDescriptorFactory;

    @Autowired
    public WebItemModuleDescriptorFactory(com.atlassian.plugin.connect.plugin.module.webitem.WebItemModuleDescriptorFactory webItemModuleDescriptorFactory)
    {
        this.webItemModuleDescriptorFactory = webItemModuleDescriptorFactory;
    }

    public WebItemModuleDescriptor createWebItemDescriptor(Plugin plugin, WebItemCapabilityBean bean, Map<String,String> contextParams)
    {
        Element webItemElement = new DOMElement("web-item");
        
        final String webItemKey = "webitem-" + bean.getKey();
        
        webItemElement.addAttribute("key", webItemKey);
        webItemElement.addAttribute("section",bean.getSection());
        webItemElement.addAttribute("weight", Integer.toString(bean.getWeight()));
        
        webItemElement.addElement("label")
                      .addAttribute("key", escapeHtml(bean.getName().getI18n()))
                      .setText(escapeHtml(bean.getName().getDefaultValue()));
        
        Element linkElement = webItemElement.addElement("link").addAttribute("linkId", webItemKey);

        String url = "";
        if (bean.getLink() != null)
        {
            if (bean.isAbsolute())
            {
                url = bean.getLink();
            }
            else
            {
                UriBuilder uriBuilder = new UriBuilder(Uri.parse("/plugins/servlet" + bean.getLink()));

                for (Map.Entry<String,String> entry : contextParams.entrySet())
                {
                    uriBuilder.addQueryParameter(entry.getKey(),entry.getValue());
                }
                
                url = uriBuilder.toString();
            }

            linkElement.setText(url);
        }

        List<String> styleClasses = new ArrayList<String>();
        styleClasses.addAll(bean.getStyleClasses());

        convertIcon(plugin, bean, webItemElement);
        
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

        if(!styleClasses.isEmpty())
        {
            webItemElement.addElement("styleClass").setText(Joiner.on(" ").join(styleClasses));    
        }
        
        if (log.isDebugEnabled())
        {
            log.debug("Created web item: " + printNode(webItemElement));
        }
        
        return createWebItemDescriptor(plugin, webItemElement, webItemKey, url, bean.isAbsolute());
    }

    private WebItemModuleDescriptor createWebItemDescriptor(Plugin plugin, Element webItemElement, String key, String url, boolean absolute)
    {
        webItemElement.addAttribute("system", "true");
        
        final WebItemModuleDescriptor descriptor = webItemModuleDescriptorFactory.createWebItemModuleDescriptor(url, key, absolute);
        
        descriptor.init(plugin, webItemElement);
        
        return descriptor;
    }
    
    private void convertIcon(Plugin plugin, WebItemCapabilityBean bean, Element webItemElement)
    {
        IconCapabilityBean iconBean = bean.getIcon();
        
        if(null != iconBean && !Strings.isNullOrEmpty(iconBean.getUrl()))
        {
            URI iconUri = URI.create(iconBean.getUrl());
            
            webItemElement.addElement("icon")
                  .addAttribute("width", Integer.toString(iconBean.getWidth()))
                  .addAttribute("height", Integer.toString(iconBean.getHeight()))
                  .addElement("link")
                  .addText(getPermanentRedirectUrl(plugin.getKey(), iconUri));
        }
    }
    
}
