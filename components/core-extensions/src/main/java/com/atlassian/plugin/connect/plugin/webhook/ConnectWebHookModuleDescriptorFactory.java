package com.atlassian.plugin.connect.plugin.webhook;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.util.ConnectContainerUtil;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebHookModuleBean;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.api.lifecycle.ConnectModuleDescriptorFactory;
import com.atlassian.webhooks.spi.plugin.WebHookModuleDescriptor;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ConnectWebHookModuleDescriptorFactory implements ConnectModuleDescriptorFactory<WebHookModuleBean, WebHookModuleDescriptor> {
    private final ConnectContainerUtil autowireUtil;

    @Autowired
    public ConnectWebHookModuleDescriptorFactory(ConnectContainerUtil autowireUtil) {
        this.autowireUtil = autowireUtil;
    }

    @Override
    public WebHookModuleDescriptor createModuleDescriptor(WebHookModuleBean bean, ConnectAddonBean addon, Plugin plugin) {
        Element webhookElement = new DOMElement("webhook");

        webhookElement.addAttribute("key", ModuleKeyUtils.generateKey("webhook"));
        webhookElement.addAttribute("event", bean.getEvent());
        webhookElement.addAttribute("url", bean.getUrl());
        for (Map.Entry<String, String> entry : bean.getParams().entrySet()) {
            webhookElement.addElement("param")
                    .addAttribute("name", entry.getKey())
                    .addAttribute("value", entry.getValue());
        }

        WebHookModuleDescriptor descriptor = autowireUtil.createBean(WebHookModuleDescriptor.class);
        descriptor.setWebhookPluginKey(addon.getKey());

        descriptor.init(plugin, webhookElement);

        return descriptor;
    }
}
