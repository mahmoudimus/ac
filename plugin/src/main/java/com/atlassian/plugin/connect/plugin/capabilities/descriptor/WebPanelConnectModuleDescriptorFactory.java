package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectAutowireUtil;
import com.atlassian.plugin.connect.plugin.module.webpanel.IFrameRemoteWebPanel;
import com.atlassian.plugin.web.descriptors.WebPanelModuleDescriptor;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;


@Component
public class WebPanelConnectModuleDescriptorFactory implements ConnectModuleDescriptorFactory<WebPanelCapabilityBean,WebPanelModuleDescriptor>
{
    private final ConnectAutowireUtil connectAutowireUtil;

    @Autowired
    public WebPanelConnectModuleDescriptorFactory(ConnectAutowireUtil connectAutowireUtil)
    {
        this.connectAutowireUtil = connectAutowireUtil;
    }

    @Override
    public WebPanelModuleDescriptor createModuleDescriptor(Plugin plugin, BundleContext addonBundleContext, WebPanelCapabilityBean bean)
    {
        Element domElement = createDomElement(bean, bean.getKey());
        final WebPanelModuleDescriptor descriptor = new ConnectDefaultWebPanelModuleDescriptor(connectAutowireUtil, bean, domElement);
        descriptor.init(plugin, domElement);
        return descriptor;
    }

    private Element createDomElement(WebPanelCapabilityBean bean, String webPanelKey)
    {
        String i18nKey = escapeHtml(bean.getName().getI18n());
        Element webPanelElement = new DOMElement("remote-web-panel");
        webPanelElement.addAttribute("key", webPanelKey);
        webPanelElement.addAttribute("i18n-name-key", i18nKey);
        webPanelElement.addAttribute("location", escapeHtml(bean.getLocation()));

        if (null != bean.getWeight())
        {
            webPanelElement.addAttribute("weight", Integer.toString(bean.getWeight()));
        }

        webPanelElement.addElement("label").addAttribute("key", i18nKey);
        webPanelElement.addAttribute("class", IFrameRemoteWebPanel.class.getName());
        webPanelElement.addAttribute("width", escapeHtml(bean.getLayout().getWidth()));
        webPanelElement.addAttribute("height", escapeHtml(bean.getLayout().getHeight()));
        webPanelElement.addAttribute("url", escapeHtml(bean.getUrl()));

        return webPanelElement;
    }
}
