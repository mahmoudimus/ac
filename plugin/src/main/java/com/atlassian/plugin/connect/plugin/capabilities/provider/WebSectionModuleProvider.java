package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.WebSectionModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebSectionModuleMeta;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectWebSectionModuleDescriptorFactory;

import com.atlassian.plugin.connect.spi.module.provider.AbstractConnectModuleProvider;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProviderContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class WebSectionModuleProvider extends AbstractConnectModuleProvider<WebSectionModuleBean>
{
    private final ConnectWebSectionModuleDescriptorFactory webSectionFactory;

    @Autowired
    public WebSectionModuleProvider(ConnectWebSectionModuleDescriptorFactory webSectionFactory)
    {
        this.webSectionFactory = webSectionFactory;
    }

    @Override
    public List<ModuleDescriptor> provideModules(ConnectModuleProviderContext moduleProviderContext, Plugin theConnectPlugin, List<WebSectionModuleBean> beans)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<>();

        for (WebSectionModuleBean bean : beans)
        {
            descriptors.addAll(beanToDescriptors(moduleProviderContext, theConnectPlugin, bean));
        }

        return descriptors;
    }

    private Collection<? extends ModuleDescriptor> beanToDescriptors(ConnectModuleProviderContext moduleProviderContext,
                                                                     Plugin theConnectPlugin, WebSectionModuleBean bean)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<>();

        descriptors.add(webSectionFactory.createModuleDescriptor(moduleProviderContext, theConnectPlugin, bean));

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
        return new WebSectionModuleMeta();
    }
}
