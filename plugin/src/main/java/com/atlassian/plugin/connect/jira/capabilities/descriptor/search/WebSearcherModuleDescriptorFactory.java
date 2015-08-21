package com.atlassian.plugin.connect.jira.capabilities.descriptor.search;

import com.atlassian.jira.search.WebSearcherModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.WebSearcherModuleBean;
import com.atlassian.plugin.connect.spi.capabilities.descriptor.ConnectModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProviderContext;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import org.springframework.beans.factory.annotation.Autowired;

@JiraComponent
public class WebSearcherModuleDescriptorFactory implements ConnectModuleDescriptorFactory<WebSearcherModuleBean, WebSearcherModuleDescriptor> {
    private final ModuleFactory moduleFactory;
    private final WebSearchRequestSender webSearchRequestSender;

    @Autowired
    public WebSearcherModuleDescriptorFactory(final ModuleFactory moduleFactory, final WebSearchRequestSender webSearchRequestSender) {
        this.moduleFactory = moduleFactory;
        this.webSearchRequestSender = webSearchRequestSender;
    }

    @Override
    public WebSearcherModuleDescriptor createModuleDescriptor(final ConnectModuleProviderContext moduleProviderContext, final Plugin plugin, final WebSearcherModuleBean bean) {
        return new ConnectWebSearcherModuleDescriptor(webSearchRequestSender, moduleFactory, moduleProviderContext, plugin, bean);
    }
}
