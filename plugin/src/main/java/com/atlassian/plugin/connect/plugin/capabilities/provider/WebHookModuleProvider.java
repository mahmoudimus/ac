package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.WebHookModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleMeta;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectWebHookModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.module.provider.AbstractConnectModuleProvider;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProviderContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class WebHookModuleProvider extends AbstractConnectModuleProvider<WebHookModuleBean>
{
    private ConnectWebHookModuleDescriptorFactory connectWebHookModuleDescriptorFactory;

    @Autowired
    public WebHookModuleProvider(ConnectWebHookModuleDescriptorFactory connectWebHookModuleDescriptorFactory)
    {
        this.connectWebHookModuleDescriptorFactory = connectWebHookModuleDescriptorFactory;
    }

    @Override
    public List<ModuleDescriptor> provideModules(ConnectModuleProviderContext moduleProviderContext, Plugin theConnectPlugin, List<WebHookModuleBean> beans)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<>();

        for (WebHookModuleBean bean: beans)
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

    @Override
    public String getSchemaPrefix()
    {
        return "common";
    }

    @Override
    public ConnectModuleMeta getMeta()
    {
        return new WebItemModuleMeta();
    }
}
