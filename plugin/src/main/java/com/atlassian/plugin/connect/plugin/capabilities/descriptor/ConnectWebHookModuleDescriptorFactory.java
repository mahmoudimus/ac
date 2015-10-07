package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.capabilities.descriptor.ParamsModuleFragmentFactory;
import com.atlassian.plugin.connect.api.capabilities.util.ConnectContainerUtil;
import com.atlassian.plugin.connect.modules.beans.WebHookModuleBean;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.spi.capabilities.descriptor.ConnectModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProviderContext;
import com.atlassian.webhooks.spi.plugin.WebHookModuleDescriptor;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConnectWebHookModuleDescriptorFactory implements ConnectModuleDescriptorFactory<WebHookModuleBean, WebHookModuleDescriptor>
{
    private final ParamsModuleFragmentFactory paramsModuleFragmentFactory;
    private final ConnectContainerUtil autowireUtil;

    @Autowired
    public ConnectWebHookModuleDescriptorFactory(ParamsModuleFragmentFactory paramsModuleFragmentFactory, ConnectContainerUtil autowireUtil)
    {
        this.paramsModuleFragmentFactory = paramsModuleFragmentFactory;
        this.autowireUtil = autowireUtil;
    }

    @Override
    public WebHookModuleDescriptor createModuleDescriptor(ConnectModuleProviderContext moduleProviderContext, Plugin theConnectPlugin, WebHookModuleBean bean)
    {
        Element webhookElement = new DOMElement("webhook");

        webhookElement.addAttribute("key", ModuleKeyUtils.generateKey("webhook"));
        webhookElement.addAttribute("event", bean.getEvent());
        webhookElement.addAttribute("url", bean.getUrl());
        paramsModuleFragmentFactory.addParamsToElement(webhookElement, bean.getParams());


        WebHookModuleDescriptor descriptor = autowireUtil.createBean(WebHookModuleDescriptor.class);
        descriptor.setWebhookPluginKey(moduleProviderContext.getConnectAddonBean().getKey());
        
        descriptor.init(theConnectPlugin, webhookElement);

        return descriptor;
    }

}
