package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import java.net.URI;
import java.net.URISyntaxException;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginState;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebHookModuleBean;
import com.atlassian.plugin.connect.modules.util.ModuleKeyGenerator;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectContainerUtil;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonToPluginFactory;
import com.atlassian.plugin.connect.plugin.module.webhook.RemotablePluginsPluginUriResolver;
import com.atlassian.webhooks.spi.plugin.WebHookModuleDescriptor;

import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConnectWebHookModuleDescriptorFactory implements ConnectModuleDescriptorFactory<WebHookModuleBean, WebHookModuleDescriptor>
{
    private static final Logger log = LoggerFactory.getLogger(ConnectWebHookModuleDescriptorFactory.class);
    
    private final ParamsModuleFragmentFactory paramsModuleFragmentFactory;
    private final ConnectContainerUtil autowireUtil;
    private final RemotablePluginsPluginUriResolver uriResolver;
    private final ConnectAddonToPluginFactory addonToPluginFactory;

    @Autowired
    public ConnectWebHookModuleDescriptorFactory(ParamsModuleFragmentFactory paramsModuleFragmentFactory, ConnectContainerUtil autowireUtil, RemotablePluginsPluginUriResolver uriResolver, ConnectAddonToPluginFactory addonToPluginFactory)
    {
        this.paramsModuleFragmentFactory = paramsModuleFragmentFactory;
        this.autowireUtil = autowireUtil;
        this.uriResolver = uriResolver;
        this.addonToPluginFactory = addonToPluginFactory;
    }

    @Override
    public WebHookModuleDescriptor createModuleDescriptor(ConnectAddonBean addon, Plugin theConnectPlugin, WebHookModuleBean bean)
    {
        Element webhookElement = new DOMElement("webhook");

        webhookElement.addAttribute("key", ModuleKeyGenerator.generateKey("webhook"));
        webhookElement.addAttribute("event", bean.getEvent());
        
//        //IMPORTANT! We need to resolve the url here becaue once the webhook fires, we no longer have the addon's key
//        URI uri;
//        try
//        {
//            uri = uriResolver.getUri(addon.getKey(),new URI(bean.getUrl())).get();
//        }
//        catch (URISyntaxException e)
//        {
//            //this should never happen
//            throw new RuntimeException("unable to parse webhook url as a uri: " + addon.getKey() + ":" + bean.getUrl(),e);
//        }
//        
//        log.debug("setting webhook descriptor url to: " + uri.toString());
        webhookElement.addAttribute("url", bean.getUrl());
        paramsModuleFragmentFactory.addParamsToElement(webhookElement, bean.getParams());


        WebHookModuleDescriptor descriptor = autowireUtil.createBean(WebHookModuleDescriptor.class);
        descriptor.setWebhookPluginKey(addon.getKey());
        
        descriptor.init(theConnectPlugin, webhookElement);

        return descriptor;
    }

}
