package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.applinks.spi.link.MutatingApplicationLinkService;
import com.atlassian.applinks.spi.util.TypeAccessor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.OAuthLinkManager;
import com.atlassian.plugin.connect.plugin.PermissionManager;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.OAuthBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.RemoteContainerCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectAutowireUtil;
import com.atlassian.plugin.connect.plugin.module.applinks.RemotePluginContainerModuleDescriptor;
import com.atlassian.plugin.connect.spi.ConnectAddOnIdentifierService;
import com.atlassian.plugin.module.ContainerManagedPlugin;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.osgi.factory.OsgiPlugin;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

import com.google.common.base.Strings;

import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RemoteContainerModuleDescriptorFactory implements ConnectModuleDescriptorFactory<RemoteContainerCapabilityBean,RemotePluginContainerModuleDescriptor>
{
    private final ConnectAutowireUtil autowireUtil;

    @Autowired
    public RemoteContainerModuleDescriptorFactory(ConnectAutowireUtil autowireUtil)
    {
        this.autowireUtil = autowireUtil;
    }

    @Override
    public RemotePluginContainerModuleDescriptor createModuleDescriptor(Plugin plugin, BundleContext addonBundleContext, RemoteContainerCapabilityBean bean)
    {
        Element containerElement = new DOMElement("remote-plugin-container");
        
        containerElement.addAttribute("key","remote-container")
                .addAttribute("display-url",bean.getDisplayUrl());

        OAuthBean oauthBean = bean.getOauth();
        
        if(null != oauthBean && !Strings.isNullOrEmpty(oauthBean.getPublicKey()))
        {
            Element oauthElement = new DOMElement("oauth");
            oauthElement.addElement("public-key").setText(oauthBean.getPublicKey());
            
            if(!Strings.isNullOrEmpty(oauthBean.getCallback()))
            {
                oauthElement.addAttribute("callback",oauthBean.getCallback());
            }

            if(!Strings.isNullOrEmpty(oauthBean.getRequestTokenUrl()))
            {
                oauthElement.addAttribute("request-token-url",oauthBean.getRequestTokenUrl());
            }

            if(!Strings.isNullOrEmpty(oauthBean.getAccessTokenUrl()))
            {
                oauthElement.addAttribute("access-token-url",oauthBean.getAccessTokenUrl());
            }

            if(!Strings.isNullOrEmpty(oauthBean.getAuthorizeUrl()))
            {
                oauthElement.addAttribute("authorize-url",oauthBean.getAuthorizeUrl());
            }
            
            containerElement.add(oauthElement);
        }

       
        //RemotePluginContainerModuleDescriptor descriptor = new RemotePluginContainerModuleDescriptor(moduleFactory,applicationLinkService,oAuthLinkManager,permissionManager,typeAccessor,addonBundleContext,pluginSettingsFactory,connectIdentifier);
        RemotePluginContainerModuleDescriptor descriptor = autowireUtil.createBean(RemotePluginContainerModuleDescriptor.class);
        descriptor.init(plugin,containerElement);
        
        return descriptor;
        
    }
}
