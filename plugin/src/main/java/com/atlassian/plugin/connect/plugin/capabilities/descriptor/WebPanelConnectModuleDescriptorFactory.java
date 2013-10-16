package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebPanelCapabilityBean;
import com.atlassian.plugin.module.ContainerManagedPlugin;
import com.atlassian.plugin.web.descriptors.WebPanelModuleDescriptor;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.osgi.framework.BundleContext;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;


@Component
public class WebPanelConnectModuleDescriptorFactory implements ConnectModuleDescriptorFactory<WebPanelCapabilityBean,WebPanelModuleDescriptor>
{
    @Override
    public WebPanelModuleDescriptor createModuleDescriptor(Plugin plugin, BundleContext addonBundleContext, WebPanelCapabilityBean bean)
    {
        final WebPanelModuleDescriptor descriptor = ((ContainerManagedPlugin)plugin).getContainerAccessor().createBean(WebPanelModuleDescriptor.class);
        descriptor.init(plugin, createDomElement(bean, bean.getKey()));
        return descriptor;
    }

    private Element createDomElement(WebPanelCapabilityBean bean, String webPanelKey)
    {
        Element webPanelElement = new DOMElement("remote-web-panel");
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

        webPanelElement.addAttribute("system", "true");

        return webPanelElement;
    }
}
