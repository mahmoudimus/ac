package com.atlassian.plugin.connect.jira.search;

import com.atlassian.jira.search.WebSearcher;
import com.atlassian.jira.search.WebSearcherModuleDescriptor;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;

public class ConnectWebSearcherModuleDescriptor extends AbstractModuleDescriptor<WebSearcher> implements WebSearcherModuleDescriptor
{
    private final ConnectWebSearcher searcher;

    public ConnectWebSearcherModuleDescriptor(ModuleFactory moduleFactory, ConnectWebSearcher webSearcher)
    {
        super(moduleFactory);
        this.searcher = webSearcher;
    }

    @Override
    public WebSearcher getModule()
    {
        return searcher;
    }
}
