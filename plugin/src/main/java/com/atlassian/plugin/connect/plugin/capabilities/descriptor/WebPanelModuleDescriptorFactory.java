package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.spi.module.DynamicMarkerCondition;
import com.atlassian.plugin.web.descriptors.WebPanelModuleDescriptor;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.*;
import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

import com.atlassian.plugin.connect.plugin.capabilities.beans.WebPanelCapabilityBean;


@Component
public class WebPanelModuleDescriptorFactory  implements ConnectModuleDescriptorFactory<WebPanelCapabilityBean,WebPanelModuleDescriptor> {

    private static final Logger log = LoggerFactory.getLogger(WebItemModuleDescriptorFactory.class);

    private final WebPanelModuleDescriptorFactory remoteWebPanelDescriptorFactory;

    @Autowired
    public WebPanelModuleDescriptorFactory(WebPanelModuleDescriptorFactory remoteWebPanelDescriptorFactory) {
        this.remoteWebPanelDescriptorFactory = remoteWebPanelDescriptorFactory;
    }


    @Override
    public WebPanelModuleDescriptor createModuleDescriptor(Plugin plugin,BundleContext addonBundleContext, WebPanelCapabilityBean bean)
    {
        Element webPanelElement = new DOMElement("remote-web-panel");

        String webPanelKey = bean.getKey();

        webPanelElement.addAttribute("key", webPanelKey);
        webPanelElement.addAttribute("location", escapeHtml(bean.getLocation()));
        webPanelElement.addAttribute("width", escapeHtml(bean.getLayout().getWidth()));
        webPanelElement.addAttribute("height", escapeHtml(bean.getLayout().getHeight()));
        webPanelElement.addAttribute("weight", Integer.toString(bean.getWeight()));
        webPanelElement.addAttribute("url", escapeHtml(bean.getUrl()));
        webPanelElement.addAttribute("state", "enabled");

        webPanelElement.addElement("label")
                .addAttribute("key", escapeHtml(bean.getName().getI18n()))
                .setText(escapeHtml(bean.getName().getValue()));


        return createWebPanelDescriptor(plugin, webPanelElement, webPanelKey, bean.getLocation(), bean.isAbsolute());
    }

    private WebPanelModuleDescriptor createWebPanelDescriptor(Plugin plugin, Element webPanelElement, String key, String url, boolean absolute)
    {
        webPanelElement.addAttribute("system", "true");

        final WebPanelModuleDescriptor descriptor = remoteWebPanelDescriptorFactory.createWebPanelModuleDescriptor(url, key, absolute);

        descriptor.init(plugin, webPanelElement);

        return descriptor;
    }


}
