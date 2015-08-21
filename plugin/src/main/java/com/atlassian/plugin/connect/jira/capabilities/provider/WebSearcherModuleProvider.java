package com.atlassian.plugin.connect.jira.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.jira.capabilities.descriptor.search.WebSearcherModuleDescriptorFactory;
import com.atlassian.plugin.connect.modules.beans.DashboardItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebSearcherModuleBean;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProvider;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProviderContext;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@JiraComponent
public class WebSearcherModuleProvider implements ConnectModuleProvider<WebSearcherModuleBean> {

    private final WebSearcherModuleDescriptorFactory webSearcherModuleDescriptorFactory;

    @Autowired
    public WebSearcherModuleProvider(final WebSearcherModuleDescriptorFactory webSearcherModuleDescriptorFactory) {
        this.webSearcherModuleDescriptorFactory = webSearcherModuleDescriptorFactory;
    }

    @Override
    public List<ModuleDescriptor> provideModules(final ConnectModuleProviderContext moduleProviderContext, final Plugin plugin, final String jsonFieldName, final List<WebSearcherModuleBean> beans) {
        return Lists.transform(beans, new Function<WebSearcherModuleBean, ModuleDescriptor>() {
            @Override
            public ModuleDescriptor apply(final WebSearcherModuleBean bean) {
                return webSearcherModuleDescriptorFactory.createModuleDescriptor(moduleProviderContext, plugin, bean);
            }
        });
    }
}
