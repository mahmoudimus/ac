package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebSectionModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectWebSectionModuleDescriptorFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class WebSectionModuleProvider implements ConnectModuleProvider<WebSectionModuleBean>
{
    private final ConnectWebSectionModuleDescriptorFactory webSectionFactory;

    @Autowired
    public WebSectionModuleProvider(ConnectWebSectionModuleDescriptorFactory webSectionFactory)
    {
        this.webSectionFactory = webSectionFactory;
    }

    @Override
    public List<ModuleDescriptor> provideModules(ConnectAddonBean addon, Plugin theConnectPlugin, String jsonFieldName, List<WebSectionModuleBean> beans)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<ModuleDescriptor>();

        for (WebSectionModuleBean bean : beans)
        {
            descriptors.addAll(beanToDescriptors(addon, theConnectPlugin, bean));
        }

        return descriptors;
    }

    private Collection<? extends ModuleDescriptor> beanToDescriptors(ConnectAddonBean addon,Plugin theConnectPlugin, WebSectionModuleBean bean)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<ModuleDescriptor>();

        descriptors.add(webSectionFactory.createModuleDescriptor(addon, theConnectPlugin, bean));

        return descriptors;
    }
}
