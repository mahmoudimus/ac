package com.atlassian.plugin.connect.core.capabilities.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.core.capabilities.descriptor.ConnectWebHookModuleDescriptorFactory;
import com.atlassian.plugin.connect.modules.beans.WebHookModuleBean;

import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProvider;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProviderContext;
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
    public List<ModuleDescriptor> provideModules(ConnectModuleProviderContext moduleProviderContext,
                                                 Plugin theConnectPlugin, String jsonFieldName, List<WebHookModuleBean> beans)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<>();

        for (WebHookModuleBean bean : beans)
        {
            descriptors.addAll(beanToDescriptors(moduleProviderContext, theConnectPlugin, bean));
        }

        return descriptors;
    }

    private Collection<? extends ModuleDescriptor> beanToDescriptors(ConnectModuleProviderContext moduleProviderContext,
                                                                     Plugin theConnectPlugin, WebHookModuleBean bean)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<>();
        descriptors.add(connectWebHookModuleDescriptorFactory.createModuleDescriptor(moduleProviderContext, theConnectPlugin, bean));

        return descriptors;
    }
}
