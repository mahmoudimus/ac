package com.atlassian.plugin.connect.plugin.capabilities.provider;

import java.util.ArrayList;
import java.util.List;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.SearchRequestViewModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.SearchRequestViewModuleDescriptorFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;

import org.springframework.beans.factory.annotation.Autowired;

@JiraComponent
public class SearchRequestViewModuleProvider implements ConnectModuleProvider<SearchRequestViewModuleBean>
{
    private final SearchRequestViewModuleDescriptorFactory searchRequestViewModuleDescriptorFactory;

    @Autowired
    public SearchRequestViewModuleProvider(SearchRequestViewModuleDescriptorFactory searchRequestViewModuleDescriptorFactory)
    {
        this.searchRequestViewModuleDescriptorFactory = searchRequestViewModuleDescriptorFactory;
    }

    @Override
    public List<ModuleDescriptor> provideModules(ConnectAddonBean addon, Plugin theConnectPlugin, String jsonFieldName, List<SearchRequestViewModuleBean> beans)
    {
        List<ModuleDescriptor> moduleDescriptors = new ArrayList<ModuleDescriptor>();

        for (SearchRequestViewModuleBean bean : beans)
        {
            ModuleDescriptor descriptor = searchRequestViewModuleDescriptorFactory.createModuleDescriptor(addon, theConnectPlugin, bean);
            moduleDescriptors.add(descriptor);
        }

        return moduleDescriptors;
    }
}
