package com.atlassian.plugin.connect.plugin.capabilities.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebHookModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectWebHookModuleDescriptorFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WebHookModuleProvider implements ConnectModuleProvider<WebHookModuleBean>
{
    private ConnectWebHookModuleDescriptorFactory connectWebHookModuleDescriptorFactory;

    @Autowired
    public WebHookModuleProvider(ConnectWebHookModuleDescriptorFactory connectWebHookModuleDescriptorFactory)
    {
        this.connectWebHookModuleDescriptorFactory = connectWebHookModuleDescriptorFactory;
    }

    @Override
    public List<ModuleDescriptor> provideModules(ConnectAddonBean addon, Plugin theConnectPlugin, String jsonFieldName, List<WebHookModuleBean> beans)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<ModuleDescriptor>();

        for (WebHookModuleBean bean : beans)
        {
            descriptors.addAll(beanToDescriptors(addon, theConnectPlugin, bean));
        }

        return descriptors;
    }

    private Collection<? extends ModuleDescriptor> beanToDescriptors(ConnectAddonBean addon,Plugin theConnectPlugin, WebHookModuleBean bean)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<ModuleDescriptor>();
        descriptors.add(connectWebHookModuleDescriptorFactory.createModuleDescriptor(addon, theConnectPlugin, bean));

        return descriptors;
    }
}
