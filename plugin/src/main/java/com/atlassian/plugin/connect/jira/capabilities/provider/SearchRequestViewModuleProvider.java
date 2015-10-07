package com.atlassian.plugin.connect.jira.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.jira.capabilities.descriptor.SearchRequestViewModuleDescriptorFactory;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.SearchRequestViewModuleBean;
import com.atlassian.plugin.connect.modules.beans.SearchRequestViewModuleMeta;
import com.atlassian.plugin.connect.spi.module.AbstractConnectModuleProvider;
import com.atlassian.plugin.connect.spi.module.ConnectModuleProviderContext;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@JiraComponent
public class SearchRequestViewModuleProvider extends AbstractConnectModuleProvider<SearchRequestViewModuleBean>
{

    private static final SearchRequestViewModuleMeta META = new SearchRequestViewModuleMeta();

    private final SearchRequestViewModuleDescriptorFactory searchRequestViewModuleDescriptorFactory;

    @Autowired
    public SearchRequestViewModuleProvider(SearchRequestViewModuleDescriptorFactory searchRequestViewModuleDescriptorFactory)
    {
        this.searchRequestViewModuleDescriptorFactory = searchRequestViewModuleDescriptorFactory;
    }

    @Override
    public List<ModuleDescriptor> createPluginModuleDescriptors(List<SearchRequestViewModuleBean> modules, final Plugin theConnectPlugin, final ConnectModuleProviderContext moduleProviderContext)
    {
        List<ModuleDescriptor> moduleDescriptors = new ArrayList<>();

        for (SearchRequestViewModuleBean bean : modules)
        {
            ModuleDescriptor descriptor = searchRequestViewModuleDescriptorFactory.createModuleDescriptor(moduleProviderContext, theConnectPlugin, bean);
            moduleDescriptors.add(descriptor);
        }

        return moduleDescriptors;
    }

    @Override
    public String getSchemaPrefix()
    {
        return "jira";
    }

    @Override
    public ConnectModuleMeta<SearchRequestViewModuleBean> getMeta()
    {
        return META;
    }
}
