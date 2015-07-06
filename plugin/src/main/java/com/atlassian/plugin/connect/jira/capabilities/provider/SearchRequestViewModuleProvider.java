package com.atlassian.plugin.connect.jira.capabilities.provider;

import java.util.ArrayList;
import java.util.List;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.SearchRequestViewModuleBean;
import com.atlassian.plugin.connect.jira.capabilities.descriptor.SearchRequestViewModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProvider;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProviderContext;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;

@JiraComponent
public class SearchRequestViewModuleProvider implements ConnectModuleProvider
{
    public static final String DESCRIPTOR_KEY = "jiraSearchRequestViews";
    public static final Class BEAN_CLASS = SearchRequestViewModuleBean.class;
    
    private final SearchRequestViewModuleDescriptorFactory searchRequestViewModuleDescriptorFactory;

    @Autowired
    public SearchRequestViewModuleProvider(SearchRequestViewModuleDescriptorFactory searchRequestViewModuleDescriptorFactory)
    {
        this.searchRequestViewModuleDescriptorFactory = searchRequestViewModuleDescriptorFactory;
    }

    @Override
    public List<ModuleDescriptor> provideModules(final ConnectModuleProviderContext moduleProviderContext, final Plugin theConnectPlugin, List<JsonObject> modules)
    {
        List<ModuleDescriptor> moduleDescriptors = new ArrayList<>();

        for (JsonObject module : modules)
        {
            SearchRequestViewModuleBean bean = new Gson().fromJson(module, SearchRequestViewModuleBean.class);
            ModuleDescriptor descriptor = searchRequestViewModuleDescriptorFactory.createModuleDescriptor(moduleProviderContext, theConnectPlugin, bean);
            moduleDescriptors.add(descriptor);
        }

        return moduleDescriptors;
    }
    
    @Override
    public Class getBeanClass()
    {
        return BEAN_CLASS;
    }

    @Override
    public String getDescriptorKey()
    {
        return DESCRIPTOR_KEY;
    }
}
