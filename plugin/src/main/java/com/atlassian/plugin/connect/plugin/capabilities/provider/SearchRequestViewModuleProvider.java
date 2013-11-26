package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.SearchRequestViewCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.SearchRequestViewModuleDescriptorFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@JiraComponent
public class SearchRequestViewModuleProvider implements ConnectModuleProvider<SearchRequestViewCapabilityBean>
{
    private final SearchRequestViewModuleDescriptorFactory searchRequestViewModuleDescriptorFactory;

    @Autowired
    public SearchRequestViewModuleProvider(SearchRequestViewModuleDescriptorFactory searchRequestViewModuleDescriptorFactory)
    {
        this.searchRequestViewModuleDescriptorFactory = searchRequestViewModuleDescriptorFactory;
    }

    @Override
    public List<ModuleDescriptor> provideModules(Plugin plugin, BundleContext addonBundleContext, String jsonFieldName, List<SearchRequestViewCapabilityBean> beans)
    {
        List<ModuleDescriptor> moduleDescriptors = new ArrayList<ModuleDescriptor>();

        for (SearchRequestViewCapabilityBean bean : beans)
        {
            ModuleDescriptor descriptor = searchRequestViewModuleDescriptorFactory.createModuleDescriptor(plugin, addonBundleContext, bean);
            moduleDescriptors.add(descriptor);
        }

        return moduleDescriptors;
    }
}
