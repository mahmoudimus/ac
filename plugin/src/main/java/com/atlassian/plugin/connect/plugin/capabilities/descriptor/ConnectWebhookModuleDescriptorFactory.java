package com.atlassian.plugin.connect.plugin.capabilities.descriptor;


import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebHookCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectAutowireUtil;
import com.atlassian.plugin.connect.plugin.capabilities.util.ModuleKeyGenerator;
import com.atlassian.webhooks.spi.plugin.WebHookModuleDescriptor;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConnectWebHookModuleDescriptorFactory implements ConnectModuleDescriptorFactory<WebHookCapabilityBean, WebHookModuleDescriptor>
{
    private final ParamsModuleFragmentFactory paramsModuleFragmentFactory;
    private final ConnectAutowireUtil autowireUtil;

    @Autowired
    public ConnectWebHookModuleDescriptorFactory(ParamsModuleFragmentFactory paramsModuleFragmentFactory, ConnectAutowireUtil autowireUtil)
    {
        this.paramsModuleFragmentFactory = paramsModuleFragmentFactory;
        this.autowireUtil = autowireUtil;
    }

    @Override
    public WebHookModuleDescriptor createModuleDescriptor(Plugin plugin, BundleContext addonBundleContext, WebHookCapabilityBean bean)
    {
        Element webhookElement = new DOMElement("webhook");

        webhookElement.addAttribute("key", ModuleKeyGenerator.generateKey("webhook"));
        webhookElement.addAttribute("event", bean.getEvent());
        webhookElement.addAttribute("url", bean.getUrl());
        paramsModuleFragmentFactory.addParamsToElement(webhookElement, bean.getParams());


        WebHookModuleDescriptor descriptor = autowireUtil.createBean(WebHookModuleDescriptor.class);
        descriptor.init(plugin, webhookElement);

        return descriptor;
    }

}
