package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.AutowireCapablePlugin;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.module.webpanel.IFrameRemoteWebPanel;
import com.atlassian.plugin.hostcontainer.DefaultHostContainer;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.DefaultWebPanelModuleDescriptor;
import com.atlassian.plugin.web.descriptors.WebPanelModuleDescriptor;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;


@Component
public class WebPanelConnectModuleDescriptorFactory implements ConnectModuleDescriptorFactory<WebPanelCapabilityBean,WebPanelModuleDescriptor>
{
    private static final Logger log = LoggerFactory.getLogger(WebItemModuleDescriptorFactory.class);

    @Override
    public WebPanelModuleDescriptor createModuleDescriptor(Plugin plugin, BundleContext addonBundleContext, WebPanelCapabilityBean bean)
    {
        Element domElement = createDomElement(bean, bean.getKey());
        //AlwaysDisplayCondition condition = new AlwaysDisplayCondition(); // bean.getCondition() != null ? bean.getCondition() : new AlwaysDisplayCondition();
        //IFrameContext iFrameContext = new IFrameContextImpl(plugin.getKey(), bean.getUrl(), bean.getKey(), new IFrameParamsImpl(domElement));
        //IFrameRenderer iFrameRenderer = new IFrameRendererImpl(templateRenderer, iFrameHost, remotablePluginAccessorFactory, userPreferencesRetriever, licenseRetriever, localeHelper, userManager);

        //final IFrameRemoteWebPanel remoteWebPanel = new IFrameRemoteWebPanel(iFrameRenderer, iFrameContext, condition, contextMapURLSerializer, userManager, new UrlVariableSubstitutor());
        final IFrameRemoteWebPanel remoteWebPanel = ((AutowireCapablePlugin) plugin).autowire(IFrameRemoteWebPanel.class);

        ModuleFactory moduleFactory = new ModuleFactory()
        {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T createModule(String name, ModuleDescriptor<T> moduleDescriptor) throws PluginParseException
            {
                if (!moduleDescriptor.getModule().getClass().isInstance(remoteWebPanel))
                {
                    String message = String.format("Cannot to convert %s to %s", remoteWebPanel, moduleDescriptor.getModule().getClass().getCanonicalName());
                    log.error(message);
                    throw new IllegalArgumentException(message);
                }

                return (T) remoteWebPanel;
            }
        };

        WebInterfaceManager webInterfaceManager = ((AutowireCapablePlugin)plugin).autowire(WebInterfaceManager.class); //getService(addonBundleContext, WebInterfaceManager.class);
        final WebPanelModuleDescriptor descriptor = new DefaultWebPanelModuleDescriptor(new DefaultHostContainer(), moduleFactory, webInterfaceManager);
        descriptor.init(plugin, domElement);
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

        webPanelElement.element("resource");

        return webPanelElement;
    }
}
