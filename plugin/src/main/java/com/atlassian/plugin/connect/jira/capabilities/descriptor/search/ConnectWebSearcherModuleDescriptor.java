package com.atlassian.plugin.connect.jira.capabilities.descriptor.search;

import com.atlassian.jira.search.SearchResultItem;
import com.atlassian.jira.search.WebSearcher;
import com.atlassian.jira.search.WebSearcherModuleDescriptor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.WebSearcherModuleBean;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProviderContext;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;

import java.util.Collections;
import java.util.List;

public class ConnectWebSearcherModuleDescriptor extends AbstractModuleDescriptor<WebSearcher> implements WebSearcherModuleDescriptor {

    private final ConnectWebSearcher searcher;
    private final WebSearchRequestSender sender;

    public ConnectWebSearcherModuleDescriptor(WebSearchRequestSender webSearchRequestSender, ModuleFactory moduleFactory, final ConnectModuleProviderContext moduleProviderContext, final Plugin plugin, final WebSearcherModuleBean bean) {
        super(moduleFactory);
        this.sender = webSearchRequestSender;
        String baseUrl = moduleProviderContext.getConnectAddonBean().getBaseUrl();
        String key = bean.getKey(moduleProviderContext.getConnectAddonBean());
        this.searcher = new ConnectWebSearcher(key, bean.getCategoryName(), baseUrl + bean.getUrl(), key.split("_")[0]);
    }

    class ConnectWebSearcher implements WebSearcher {
        private final String key;
        private final String categoryName;
        private final String url;
        private final String pluginKey;

        public ConnectWebSearcher(final String key, final String categoryName, final String url, final String pluginKey) {
            this.key = key;
            this.categoryName = categoryName;
            this.url = url;
            this.pluginKey = pluginKey;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getCategoryName() {
            return categoryName;
        }

        public String getUrl() {
            return url;
        }

        public String getPluginKey() {
            return pluginKey;
        }

        @Override
        public List<SearchResultItem> search(final ApplicationUser applicationUser, final String query) {
            return sender.send(this, query);
        }

        @Override
        public List<SearchResultItem> getRecentItems(final ApplicationUser applicationUser) {
            return Collections.emptyList();
        }
    }

    @Override
    public WebSearcher getModule() {
        return searcher;
    }
}
