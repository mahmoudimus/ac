package com.atlassian.plugin.connect.jira.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.jira.capabilities.descriptor.SearchRequestViewModuleDescriptorFactory;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.SearchRequestViewModuleBean;
import com.atlassian.plugin.connect.modules.beans.SearchRequestViewModuleMeta;
import com.atlassian.plugin.connect.spi.module.ConnectModuleProviderContext;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@JiraComponent
public class SearchRequestViewModuleProvider extends AbstractJiraConnectModuleProvider<SearchRequestViewModuleBean>
{

    private static final SearchRequestViewModuleMeta META = new SearchRequestViewModuleMeta();

    private final SearchRequestViewModuleDescriptorFactory searchRequestViewModuleDescriptorFactory;

    @Autowired
    public SearchRequestViewModuleProvider(PluginRetrievalService pluginRetrievalService,
            ConnectJsonSchemaValidator schemaValidator,
            SearchRequestViewModuleDescriptorFactory searchRequestViewModuleDescriptorFactory)
    {
        super(pluginRetrievalService, schemaValidator);
        this.searchRequestViewModuleDescriptorFactory = searchRequestViewModuleDescriptorFactory;
    }

    @Override
    public List<ModuleDescriptor> createPluginModuleDescriptors(List<SearchRequestViewModuleBean> modules, final ConnectModuleProviderContext moduleProviderContext)
    {
        List<ModuleDescriptor> moduleDescriptors = new ArrayList<>();

        for (SearchRequestViewModuleBean bean : modules)
        {
            ModuleDescriptor descriptor = searchRequestViewModuleDescriptorFactory.createModuleDescriptor(
                    moduleProviderContext, pluginRetrievalService.getPlugin(), bean);
            moduleDescriptors.add(descriptor);
        }

        return moduleDescriptors;
    }

    @Override
    public ConnectModuleMeta<SearchRequestViewModuleBean> getMeta()
    {
        return META;
    }
}
